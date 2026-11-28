package com.example.gemini.data.deviceaccess.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.gemini.data.deviceaccess.DeviceAccessException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import rikka.shizuku.Shizuku
import java.util.concurrent.atomic.AtomicBoolean

/** Lifecycle and authorization boundary for the official Shizuku API. */
object ShizukuAccessManager {
    private const val TAG = "IntelliShizuku"
    private const val PERMISSION_REQUEST = 0x1D41
    private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    private lateinit var app: Context
    @Volatile private var userService: IDeviceCommandService? = null
    private val binding = AtomicBoolean(false)
    @Volatile var binderAvailable: Boolean = false
        private set
    @Volatile var permissionGranted: Boolean = false
        private set
    @Volatile var lastError: String? = null
        private set

    private val binderReceived = Shizuku.OnBinderReceivedListener {
        binderAvailable = Shizuku.pingBinder()
        refreshPermission()
        if (permissionGranted) bindUserService()
    }
    private val binderDead = Shizuku.OnBinderDeadListener {
        binderAvailable = false
        permissionGranted = false
        userService = null
        binding.set(false)
        lastError = "Shizuku stopped. Start it again and reconnect."
    }
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { code, result ->
        if (code == PERMISSION_REQUEST) {
            permissionGranted = result == PackageManager.PERMISSION_GRANTED
            if (permissionGranted) bindUserService()
            else {
                lastError = "Shizuku permission was denied or revoked."
                stopUserService()
            }
        }
    }

    @Synchronized fun init(context: Context) {
        if (::app.isInitialized) return
        app = context.applicationContext
        Shizuku.addBinderReceivedListenerSticky(binderReceived)
        Shizuku.addBinderDeadListener(binderDead)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        binderAvailable = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        refreshPermission()
        if (permissionGranted) bindUserService()
    }

    fun isInstalled(): Boolean = if (!::app.isInitialized) false else runCatching {
        if (Build.VERSION.SDK_INT >= 33) app.packageManager.getPackageInfo(SHIZUKU_PACKAGE, PackageManager.PackageInfoFlags.of(0))
        else @Suppress("DEPRECATION") app.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
        true
    }.getOrDefault(false)

    fun refreshPermission(): Boolean {
        permissionGranted = binderAvailable && runCatching {
            !Shizuku.isPreV11 && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
        return permissionGranted
    }

    fun requestPermission() {
        if (!binderAvailable) {
            lastError = "Start Shizuku first. On Android 11+, use Wireless debugging; root users can start it through their root manager."
            return
        }
        if (!refreshPermission()) Shizuku.requestPermission(PERMISSION_REQUEST)
    }

    private fun stopUserService() {
        val args = serviceArgs
        if (args != null) runCatching { Shizuku.unbindUserService(args, connection, true) }
        userService = null
        serviceArgs = null
        binding.set(false)
    }

    @Volatile private var serviceArgs: Shizuku.UserServiceArgs? = null

    private fun bindUserService() {
        if (!::app.isInitialized || !binderAvailable || !permissionGranted || userService != null || !binding.compareAndSet(false, true)) return
        val args = Shizuku.UserServiceArgs(ComponentName(app, DeviceCommandUserService::class.java))
            .processName("${app.packageName}:device-access")
            .tag("intelli-device-access")
            .daemon(false)
        serviceArgs = args
        try {
            Shizuku.bindUserService(args, connection)
        } catch (error: Throwable) {
            binding.set(false)
            lastError = error.message ?: "Could not start the Shizuku command service."
            Log.e(TAG, "User service bind failed", error)
        }
    }

    val commandServiceReady: Boolean get() = userService != null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            userService = IDeviceCommandService.Stub.asInterface(service)
            binding.set(false)
            lastError = null
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
            binding.set(false)
            lastError = "Shizuku command service disconnected."
        }
    }

    suspend fun execute(id: String, command: String, timeoutSeconds: Long): JSONObject = withContext(Dispatchers.IO) {
        if (!binderAvailable || !refreshPermission()) throw DeviceAccessException("Shizuku is not connected or its permission was revoked.")
        if (userService == null) bindUserService()
        val endpoint = userService ?: throw DeviceAccessException(lastError ?: "Shizuku command service is connecting. Try again shortly.")
        val args = JSONArray().put("/system/bin/sh").put("-c").put(command)
        val request = JSONObject().put("id", id).put("args", args)
            .put("timeoutMs", timeoutSeconds.coerceIn(1, 900) * 1_000)
        try {
            JSONObject(endpoint.execute(request.toString()))
        } catch (error: Throwable) {
            userService = null
            binding.set(false)
            throw DeviceAccessException(error.message ?: "Shizuku execution failed.", error)
        }
    }

    fun cancel(id: String) {
        runCatching { userService?.cancel(id) }.onFailure { Log.w(TAG, "Shizuku cancellation failed", it) }
    }

    fun statusDetail(): String = when {
        !isInstalled() -> "Shizuku is not installed. Install it from the official project and start it."
        !binderAvailable -> "Shizuku is installed but not running. Start it through Wireless debugging or a supported root manager."
        !permissionGranted -> "Shizuku is running; Intelli permission is required."
        userService == null -> lastError ?: "Shizuku permission is granted; connecting its command service."
        Shizuku.getUid() == 0 -> "Authorized with root UID."
        Shizuku.getUid() == 2000 -> "Authorized with Android shell UID (ADB-level; not root)."
        else -> "Authorized UID ${Shizuku.getUid()}."
    }
}
