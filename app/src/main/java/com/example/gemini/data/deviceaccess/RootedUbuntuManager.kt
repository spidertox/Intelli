package com.example.gemini.data.deviceaccess

import android.content.Context
import android.os.Build
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap

/** Ubuntu Base installer and isolated chroot command runner. Never evaluates downloaded scripts. */
object RootedUbuntuManager {
    private const val ARCHIVE_NAME = "ubuntu-base-24.04.5-base-arm64.tar.gz"
    private const val ARCHIVE_URL = "https://cdimage.ubuntu.com/ubuntu-base/releases/24.04/release/$ARCHIVE_NAME"
    // Pinned from Ubuntu's signed SHA256SUMS for Ubuntu Base 24.04.5 arm64.
    private const val EXPECTED_SHA256 = "a91d5a93010193712d346d761372b7c9db6dfcf093893161c64ca107f05914f2"
    private val active = ConcurrentHashMap<String, Process>()

    fun rootfs(context: Context) = File(context.filesDir, "device-access/ubuntu-chroot/rootfs")
    private fun staging(context: Context) = File(context.filesDir, "device-access/ubuntu-chroot/rootfs.new")
    private fun archive(context: Context) = File(context.filesDir, "device-access/ubuntu-chroot/$ARCHIVE_NAME")

    fun supportedArchitecture(): Boolean = Build.SUPPORTED_ABIS.any { it == "arm64-v8a" }

    fun isInstalled(context: Context): Boolean {
        val root = rootfs(context)
        return root.isDirectory && (File(root, "bin/sh").exists() || File(root, "usr/bin/bash").exists()) && File(root, "etc/os-release").exists()
    }

    fun diskUsageBytes(context: Context): Long = rootfs(context).walkTopDown().filter { it.isFile }.sumOf { it.length() }

    suspend fun install(context: Context, approved: Boolean, onProgress: (Int, String) -> Unit = { _, _ -> }) = withContext(Dispatchers.IO) {
        require(approved) { "Ubuntu chroot installation requires explicit confirmation." }
        check(supportedArchitecture()) { "This verified Ubuntu rootfs is arm64 only." }
        val app = context.applicationContext
        val parent = archive(app).parentFile!!.apply { mkdirs() }
        check(StatFs(parent.absolutePath).availableBytes >= 512L * 1024 * 1024) { "At least 512 MB of free private storage is required for Ubuntu installation." }
        val download = archive(app)
        if (!download.isFile || sha256(download) != EXPECTED_SHA256) {
            download.delete()
            withContext(Dispatchers.Main) { onProgress(0, "Downloading official Ubuntu Base arm64 rootfs") }
            val connection = (URL(ARCHIVE_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000; readTimeout = 60_000; instanceFollowRedirects = true
            }
            try {
                check(connection.responseCode in 200..299) { "Ubuntu download failed: HTTP ${connection.responseCode}" }
                val total = connection.contentLengthLong
                FileOutputStream(download).use { out ->
                    connection.inputStream.use { input ->
                        val buffer = ByteArray(32 * 1024)
                        var written = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            written += count
                            check(written <= 100L * 1024 * 1024) { "Ubuntu archive exceeded the expected size limit." }
                            out.write(buffer, 0, count)
                            if (total > 0) withContext(Dispatchers.Main) { onProgress((written * 90 / total).toInt().coerceIn(0, 90), "Downloading Ubuntu rootfs") }
                        }
                    }
                }
            } catch (error: Throwable) {
                download.delete()
                throw error
            } finally {
                connection.disconnect()
            }
        }
        check(sha256(download) == EXPECTED_SHA256) { "Ubuntu rootfs SHA-256 verification failed; refusing to extract." }
        withContext(Dispatchers.Main) { onProgress(92, "Checksum verified; extracting with root authorization") }
        val root = rootfs(app)
        val newRoot = staging(app)
        val script = buildString {
            append("set -eu; ")
            append("rm -rf ").append(quote(newRoot.absolutePath)).append("; mkdir -p ").append(quote(newRoot.absolutePath)).append("; ")
            append("toybox tar -xzf ").append(quote(download.absolutePath)).append(" -C ").append(quote(newRoot.absolutePath)).append("; ")
            append("test -e ").append(quote(File(newRoot, "etc/os-release").absolutePath)).append("; ")
            append("test -e ").append(quote(File(newRoot, "bin/sh").absolutePath)).append(" -o -e ").append(quote(File(newRoot, "usr/bin/bash").absolutePath)).append("; ")
            append("chown -R 0:0 ").append(quote(newRoot.absolutePath)).append("; ")
            append("rm -rf ").append(quote(root.absolutePath)).append("; mv ").append(quote(newRoot.absolutePath)).append(' ').append(quote(root.absolutePath))
        }
        val installResult = runSu(script, 900)
        check(installResult.first == 0) { "Ubuntu extraction failed (exit ${installResult.first}): ${installResult.second.take(600)}" }
        check(isInstalled(app)) { "Ubuntu rootfs verification failed after extraction." }
        withContext(Dispatchers.Main) { onProgress(100, "Ubuntu 24.04.5 rootfs installed") }
        download.delete()
        parent
    }

    suspend fun execute(context: Context, command: String, requestId: String, timeoutSeconds: Long): DeviceCommandResult = withContext(Dispatchers.IO) {
        require(isInstalled(context)) { "Ubuntu root chroot is not installed." }
        require(supportedArchitecture()) { "This Ubuntu rootfs requires arm64." }
        val root = rootfs(context).absolutePath
        val isolatedScript = """
            set -eu
            root=${quote(root)}
            mount --make-rprivate /
            mount --bind /dev "${'$'}root/dev"
            mount -t proc proc "${'$'}root/proc"
            mount -t sysfs sysfs "${'$'}root/sys"
            trap 'umount "${'$'}root/sys" 2>/dev/null || true; umount "${'$'}root/proc" 2>/dev/null || true; umount "${'$'}root/dev" 2>/dev/null || true' EXIT
            chroot "${'$'}root" /usr/bin/env -i HOME=/root PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin TERM=xterm-256color /bin/sh -c ${quote(command)}
        """.trimIndent()
        val shell = "unshare -m /system/bin/sh -c ${quote(isolatedScript)}"
        val started = System.nanoTime()
        val (code, output, timeout) = runSu(shell, timeoutSeconds.coerceIn(1, 900), requestId)
        DeviceCommandResult(requestId, DeviceAccessProvider.UBUNTU_CHROOT, output, "", code,
            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started), timedOut = timeout)
    }

    suspend fun remove(context: Context, approved: Boolean) = withContext(Dispatchers.IO) {
        require(approved) { "Ubuntu rootfs removal requires explicit confirmation." }
        val path = rootfs(context).absolutePath
        val (code, output) = runSu("rm -rf ${quote(path)}", 120)
        check(code == 0) { "Rootfs removal failed: ${output.take(400)}" }
    }

    fun cancel(requestId: String) { active[requestId]?.let { runCatching { it.destroyForcibly() } } }

    private fun runSu(command: String, timeoutSeconds: Long, requestId: String? = null): Triple<Int, String, Boolean> {
        val process = ProcessBuilder("su", "-c", command).start()
        if (requestId != null) active[requestId] = process
        val output = StringBuilder()
        val reader = Thread {
            BufferedReader(InputStreamReader(process.inputStream)).use { stream ->
                var line: String?
                while (stream.readLine().also { line = it } != null) synchronized(output) { if (output.length < 512 * 1024) output.appendLine(line) }
            }
        }
        val errors = Thread {
            BufferedReader(InputStreamReader(process.errorStream)).use { stream ->
                var line: String?
                while (stream.readLine().also { line = it } != null) synchronized(output) { if (output.length < 512 * 1024) output.appendLine(line) }
            }
        }
        reader.start(); errors.start()
        val complete = try { process.waitFor(timeoutSeconds, TimeUnit.SECONDS) }
        catch (interrupted: InterruptedException) { process.destroyForcibly(); Thread.currentThread().interrupt(); throw interrupted }
        finally { if (requestId != null) active.remove(requestId, process) }
        if (!complete) process.destroyForcibly()
        reader.join(1_000); errors.join(1_000)
        return Triple(if (complete) process.exitValue() else 124, output.toString(), !complete)
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(32 * 1024)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun quote(value: String): String = "'" + value.replace("'", "'\\''") + "'"
}
