package com.example.gemini.data.deviceaccess

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.gemini.data.adb.AdbConnectManager
import com.example.gemini.data.local.LocalEnvironmentManager
import com.example.gemini.data.remote.RemoteServerManager
import com.example.gemini.data.preferences.AuthPreferences
import com.example.gemini.data.deviceaccess.shizuku.ShizukuAccessManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/** Provider router: a request is sent only to its selected provider; errors never trigger provider fallback. */
object DeviceAccessManager {
    private const val TAG = "DeviceAccess"
    private lateinit var app: Context
    private val jobs = ConcurrentHashMap<String, Job>()
    private val rootProcesses = ConcurrentHashMap<String, Process>()

    fun init(context: Context) {
        if (::app.isInitialized) return
        app = context.applicationContext
        AdbConnectManager.init(app)
        ShizukuAccessManager.init(app)
        DeviceAccessAuditLog.init(app)
    }

    suspend fun status(provider: DeviceAccessProvider): ProviderStatus {
        if (!::app.isInitialized) return ProviderStatus(provider, ProviderAvailability.ERROR, "Device access is not initialized.")
        return when (provider) {
            DeviceAccessProvider.NORMAL -> ProviderStatus(provider, ProviderAvailability.AVAILABLE, "App-owned terminal environment")
            DeviceAccessProvider.ADB, DeviceAccessProvider.WIRELESS_ADB -> {
                AdbConnectManager.refresh(quiet = true).join()
                val s = AdbConnectManager.state.value
                when {
                    s.adbInstalled == false -> ProviderStatus(provider, ProviderAvailability.NOT_INSTALLED, "Install adb from Device Access → ADB Connect.")
                    s.adbInstalled != true -> ProviderStatus(provider, ProviderAvailability.DISCONNECTED, "Checking adb in the selected terminal/server.")
                    s.connectedDevice != null -> ProviderStatus(provider, ProviderAvailability.AVAILABLE, "Connected: ${s.connectedDevice!!.serial}")
                    else -> ProviderStatus(provider, ProviderAvailability.DISCONNECTED, "Connect a device in Device Access → ADB Connect.")
                }
            }
            DeviceAccessProvider.SHIZUKU -> when {
                !ShizukuAccessManager.isInstalled() -> ProviderStatus(provider, ProviderAvailability.NOT_INSTALLED, ShizukuAccessManager.statusDetail())
                !ShizukuAccessManager.binderAvailable -> ProviderStatus(provider, ProviderAvailability.DISCONNECTED, ShizukuAccessManager.statusDetail())
                !ShizukuAccessManager.refreshPermission() -> ProviderStatus(provider, ProviderAvailability.PERMISSION_REQUIRED, ShizukuAccessManager.statusDetail())
                !ShizukuAccessManager.commandServiceReady -> ProviderStatus(provider, ProviderAvailability.DISCONNECTED, ShizukuAccessManager.statusDetail())
                else -> ProviderStatus(provider, ProviderAvailability.AVAILABLE, ShizukuAccessManager.statusDetail())
            }
            DeviceAccessProvider.ROOT -> when {
                !rootBinaryAvailable() -> ProviderStatus(provider, ProviderAvailability.NOT_SUPPORTED, "No su binary was found. Root is optional; other providers remain available.")
                else -> ProviderStatus(provider, ProviderAvailability.PERMISSION_REQUIRED, "su is present. Root authorization will be requested by the su manager when you run a confirmed command.")
            }
            DeviceAccessProvider.UBUNTU -> if (LocalEnvironmentManager.isUbuntuInstalled(app))
                ProviderStatus(provider, ProviderAvailability.AVAILABLE, "Ubuntu runs inside PRoot. It is not a root chroot.")
            else ProviderStatus(provider, ProviderAvailability.NOT_INSTALLED, "No Ubuntu PRoot rootfs is installed.")
            DeviceAccessProvider.UBUNTU_CHROOT -> when {
                !rootBinaryAvailable() -> ProviderStatus(provider, ProviderAvailability.NOT_SUPPORTED, "A supported su binary is required for a real Ubuntu chroot.")
                !RootedUbuntuManager.supportedArchitecture() -> ProviderStatus(provider, ProviderAvailability.NOT_SUPPORTED, "The verified rootfs currently supports arm64 devices only.")
                RootedUbuntuManager.isInstalled(app) -> ProviderStatus(provider, ProviderAvailability.AVAILABLE, "Ubuntu 24.04.5 arm64 in a mount-isolated root chroot; root privilege is required for each command.")
                else -> ProviderStatus(provider, ProviderAvailability.NOT_INSTALLED, "Install the pinned, SHA-256 verified Ubuntu Base rootfs.")
            }
            DeviceAccessProvider.SSH -> if (AuthPreferences.currentRemoteMode && RemoteServerManager.controlSession() != null)
                ProviderStatus(provider, ProviderAvailability.AVAILABLE, "Connected to configured SSH server")
            else ProviderStatus(provider, ProviderAvailability.DISCONNECTED, "Connect an SSH server in Settings → Servers.")
        }
    }

    fun requestShizukuPermission() = ShizukuAccessManager.requestPermission()

    suspend fun execute(request: DeviceCommandRequest): DeviceCommandResult {
        DeviceAccessRequestPolicy.validate(request)
        val job = currentCoroutineContext()[Job]
        if (job != null) jobs[request.id] = job
        val started = System.nanoTime()
        try {
            val result = when (request.provider) {
                DeviceAccessProvider.NORMAL -> local(request, DeviceAccessProvider.NORMAL)
                DeviceAccessProvider.UBUNTU -> {
                    if (!LocalEnvironmentManager.isUbuntuInstalled(app)) throw DeviceAccessException("Ubuntu PRoot is not installed.")
                    local(request, DeviceAccessProvider.UBUNTU)
                }
                DeviceAccessProvider.UBUNTU_CHROOT -> {
                    if (!request.privilegedApproved) throw DeviceAccessException("Ubuntu chroot commands require confirmation of the exact command.")
                    RootedUbuntuManager.execute(app, request.command, request.id, request.timeoutSeconds)
                }
                DeviceAccessProvider.ADB, DeviceAccessProvider.WIRELESS_ADB -> adb(request)
                DeviceAccessProvider.SHIZUKU -> shizuku(request)
                DeviceAccessProvider.ROOT -> root(request)
                DeviceAccessProvider.SSH -> ssh(request)
            }
            if (request.provider == DeviceAccessProvider.ROOT || request.provider == DeviceAccessProvider.SHIZUKU || request.provider == DeviceAccessProvider.UBUNTU_CHROOT) {
                DeviceAccessAuditLog.record(request.command, request.provider, result.exitCode, if (result.timedOut) "timeout" else "completed")
            }
            return result
        } catch (error: Throwable) {
            if (request.provider == DeviceAccessProvider.ROOT || request.provider == DeviceAccessProvider.SHIZUKU || request.provider == DeviceAccessProvider.UBUNTU_CHROOT) {
                DeviceAccessAuditLog.record(request.command, request.provider, null, "failed:${error.javaClass.simpleName}")
            }
            throw error
        } finally {
            jobs.remove(request.id)
            rootProcesses.remove(request.id)
        }
    }

    fun cancel(requestId: String) {
        jobs[requestId]?.cancel()
        rootProcesses[requestId]?.let { process ->
            runCatching { process.destroy() }
            if (process.isAlive) runCatching { process.destroyForcibly() }
        }
        ShizukuAccessManager.cancel(requestId)
        RootedUbuntuManager.cancel(requestId)
        LocalEnvironmentManager.cancelCommand(requestId)
        AdbConnectManager.cancelCommand(requestId)
        RemoteServerManager.cancelCommand(requestId)
    }

    private suspend fun local(request: DeviceCommandRequest, provider: DeviceAccessProvider): DeviceCommandResult {
        val result = LocalEnvironmentManager.executeCommand(request.command, app, timeoutSeconds = request.timeoutSeconds, executionId = request.id)
        val timeout = result.exitCode == 124
        return DeviceCommandResult(request.id, provider, result.output, "", result.exitCode, result.durationMs, timedOut = timeout)
    }

    private suspend fun adb(request: DeviceCommandRequest): DeviceCommandResult {
        val device = if (request.targetDeviceId == null) AdbConnectManager.ensureConnected()
        else AdbConnectManager.state.value.devices.firstOrNull { it.serial == request.targetDeviceId && it.state == "device" }
        ?: throw DeviceAccessException("Selected ADB device is disconnected.")
        val target = device ?: throw DeviceAccessException("No verified ADB device is connected.")
        val result = AdbConnectManager.deviceShell(target, request.command, request.timeoutSeconds, request.id)
        return DeviceCommandResult(request.id, request.provider, result.output, "", result.exitCode, result.durationMs, result.exitCode == 124)
    }

    private suspend fun shizuku(request: DeviceCommandRequest): DeviceCommandResult {
        val result = ShizukuAccessManager.execute(request.id, request.command, request.timeoutSeconds)
        return DeviceCommandResult(
            request.id, request.provider, result.optString("stdout"), result.optString("stderr"),
            result.optInt("exitCode", 1), result.optLong("durationMs"), result.optBoolean("timedOut")
        )
    }

    private suspend fun root(request: DeviceCommandRequest): DeviceCommandResult {
        if (!request.privilegedApproved) throw DeviceAccessException("Root execution requires explicit confirmation of the exact command.")
        return withContext(Dispatchers.IO) {
            val process = try { ProcessBuilder("su", "-c", request.command).start() }
            catch (error: Exception) { throw DeviceAccessException("Could not start su: ${error.message}", error) }
            rootProcesses[request.id] = process
            val stdout = ByteArrayOutputStream(); val stderr = ByteArrayOutputStream()
            val outThread = Thread({ drain(process.inputStream, stdout) }, "root-out-${request.id}")
            val errThread = Thread({ drain(process.errorStream, stderr) }, "root-err-${request.id}")
            outThread.start(); errThread.start()
            val started = System.nanoTime()
            val done = try { process.waitFor(request.timeoutSeconds, TimeUnit.SECONDS) }
            catch (error: InterruptedException) { process.destroyForcibly(); Thread.currentThread().interrupt(); false }
            if (!done) process.destroyForcibly()
            outThread.join(1_000); errThread.join(1_000)
            DeviceCommandResult(
                request.id, request.provider, stdout.toString(Charsets.UTF_8.name()), stderr.toString(Charsets.UTF_8.name()),
                if (done) process.exitValue() else 124, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started), timedOut = !done
            )
        }
    }

    private suspend fun ssh(request: DeviceCommandRequest): DeviceCommandResult = withContext(Dispatchers.IO) {
        if (!AuthPreferences.currentRemoteMode) throw DeviceAccessException("SSH provider requires an active configured server session.")
        val session = RemoteServerManager.controlSession() ?: throw DeviceAccessException("SSH server is disconnected.")
        val started = System.nanoTime()
        val (code, output) = RemoteServerManager.exec(session, request.command, request.timeoutSeconds * 1_000, request.id)
        DeviceCommandResult(request.id, request.provider, output, "", code, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started), code == 124)
    }

    private fun rootBinaryAvailable(): Boolean = listOf("/system/bin/su", "/system/xbin/su", "/sbin/su").any { java.io.File(it).canExecute() }

    private fun drain(input: InputStream, output: ByteArrayOutputStream) {
        input.use { stream ->
            val buffer = ByteArray(8_192)
            while (true) {
                val n = stream.read(buffer)
                if (n < 0) break
                synchronized(output) {
                    val remaining = 1024 * 1024 - output.size()
                    if (remaining > 0) output.write(buffer, 0, minOf(n, remaining))
                }
            }
        }
    }
}
