package com.krcast.sender.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.preferencesDataStore
import com.krcast.sender.domain.repository.PreferenceRepository

class PreferenceRepositoryImpl(private val context: Context) : PreferenceRepository {

    private val dataStore by lazy { createDataStore("krcast_prefs") }

    private val THEME_DARK = preferencesKey<Boolean>("theme_dark")
    private val AUTO_SCAN = preferencesKey<Boolean>("auto_scan")

    override val isDarkTheme: Boolean
        get() = dataStore.data.map { it[THEME_DARK] ?: true }
            .first()
            .orAwait()

    override fun saveThemePreference(isDark: Boolean) {
        dataStore.edit { it[THEME_DARK] = isDark }
    }

    override val isAutoScan: Boolean
        get() = dataStore.data.map { it[AUTO_SCAN] ?: false }
            .first()
            .orAwait()

    override fun saveAutoScanPreference(autoScan: Boolean) {
        dataStore.edit { it[AUTO_SCAN] = autoScan }
    }
}
