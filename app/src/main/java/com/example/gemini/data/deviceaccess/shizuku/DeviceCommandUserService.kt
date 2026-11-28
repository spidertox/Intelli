package com.example.gemini.data.deviceaccess.shizuku

import android.os.SystemClock
import android.util.Log
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/** Runs only requests sent over the user-authorized Shizuku binder. */
class DeviceCommandUserService : IDeviceCommandService.Stub() {
    private val processes = ConcurrentHashMap<String, Process>()

    override fun execute(requestJson: String): String {
        val request = JSONObject(requestJson)
        val id = request.getString("id")
        val args = request.getJSONArray("args").let { array ->
            List(array.length()) { index -> array.getString(index) }
        }
        val timeoutMs = request.optLong("timeoutMs", 60_000L).coerceIn(1_000L, 900_000L)
        require(args.isNotEmpty() && args.size <= 128 && args.all { it.length <= 16_384 }) { "Invalid process arguments" }
        val started = SystemClock.elapsedRealtime()
        val process = ProcessBuilder(args).start()
        processes[id] = process
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val outThread = Thread({ copy(process.inputStream, stdout) }, "device-access-out-$id")
        val errThread = Thread({ copy(process.errorStream, stderr) }, "device-access-err-$id")
        outThread.start(); errThread.start()
        var timedOut = false
        val code = try {
            if (process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) process.exitValue() else {
                timedOut = true
                process.destroy()
                if (!process.waitFor(500, TimeUnit.MILLISECONDS)) process.destroyForcibly()
                124
            }
        } catch (e: InterruptedException) {
            process.destroyForcibly()
            Thread.currentThread().interrupt()
            130
        } finally {
            processes.remove(id, process)
        }
        outThread.join(1_000); errThread.join(1_000)
        return JSONObject()
            .put("id", id)
            .put("stdout", stdout.toString(Charsets.UTF_8.name()))
            .put("stderr", stderr.toString(Charsets.UTF_8.name()))
            .put("exitCode", code)
            .put("durationMs", SystemClock.elapsedRealtime() - started)
            .put("timedOut", timedOut)
            .toString()
    }

    override fun cancel(requestId: String) {
        processes[requestId]?.destroyForcibly()
    }

    private fun copy(input: InputStream, output: ByteArrayOutputStream) {
        input.use { stream ->
            val buffer = ByteArray(8_192)
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                synchronized(output) {
                    val remaining = 128 * 1024 - output.size()
                    if (remaining > 0) output.write(buffer, 0, minOf(count, remaining))
                }
            }
        }
    }

    override fun destroy() {
        processes.values.forEach { runCatching { it.destroyForcibly() } }
        processes.clear()
        Log.i("DeviceCommandService", "User service stopped")
        // Shizuku documents that unbindUserService(remove = true) invokes this hook but
        // does not otherwise guarantee that the user-service process exits.
        System.exit(0)
    }
}
