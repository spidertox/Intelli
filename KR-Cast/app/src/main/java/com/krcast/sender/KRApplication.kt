package com.krcast.sender

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KRApplication : Application() {

    val dataStore by lazy {
        createDataStore(name = "krcast_preferences")
    }

    val isDarkTheme: StateFlow<Boolean> by lazy {
        dataStore.data.map { preferences ->
            preferences[THEME_DARK_KEY] ?: true
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main.immediate),
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    }

    companion object {
        private const val THEME_DARK_KEY = "theme_dark"
    }

    fun saveThemePreference(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[THEME_DARK_KEY] = isDark
        }
    }
}
