package com.example.gemini.data.deviceaccess

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/** Stores only metadata and a command digest for elevated-provider auditing; never stores command text/output. */
object DeviceAccessAuditLog {
    private const val PREFS = "device_access_audit"
    private const val KEY_ENTRIES = "entries"
    private lateinit var app: Context

    fun init(context: Context) { if (!::app.isInitialized) app = context.applicationContext }

    @Synchronized fun record(command: String, provider: DeviceAccessProvider, exitCode: Int?, outcome: String) {
        if (!::app.isInitialized || (provider != DeviceAccessProvider.ROOT && provider != DeviceAccessProvider.SHIZUKU && provider != DeviceAccessProvider.UBUNTU_CHROOT)) return
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val old = runCatching { JSONArray(prefs.getString(KEY_ENTRIES, "[]")) }.getOrDefault(JSONArray())
        val next = JSONArray()
        next.put(JSONObject()
            .put("time", System.currentTimeMillis())
            .put("provider", provider.name)
            .put("commandSha256", sha256(command))
            .put("exitCode", exitCode ?: JSONObject.NULL)
            .put("outcome", outcome.take(48)))
        for (index in 0 until minOf(old.length(), 99)) next.put(old.getJSONObject(index))
        prefs.edit().putString(KEY_ENTRIES, next.toString()).apply()
    }

    fun entries(): List<JSONObject> {
        if (!::app.isInitialized) return emptyList()
        val array = runCatching { JSONArray(app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ENTRIES, "[]")) }
            .getOrDefault(JSONArray())
        return List(array.length()) { array.getJSONObject(it) }
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
