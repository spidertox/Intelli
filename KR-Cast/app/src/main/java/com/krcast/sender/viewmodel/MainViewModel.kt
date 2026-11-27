package com.krcast.sender.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krcast.sender.domain.model.DiscoveredDevice
import com.krcast.sender.domain.model.MirroringCapability
import com.krcast.sender.domain.model.WifiP2pState
import com.krcast.sender.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = createDataStore(name = "krcast_prefs")

    private val checkCapabilities = CheckMirroringCapabilitiesUseCase(
        com.krcast.sender.data.repository.CapabilityRepositoryImpl(application)
    )
    private val observeWifiP2p = ObserveWifiP2pStateUseCase(
        com.krcast.sender.data.repository.WifiP2pRepositoryImpl(application)
    )
    private val startDiscovery = StartDiscoveryUseCase(
        com.krcast.sender.data.repository.WifiP2pRepositoryImpl(application)
    )
    private val stopDiscovery = StopDiscoveryUseCase(
        com.krcast.sender.data.repository.WifiP2pRepositoryImpl(application)
    )
    private val connectToDevice = ConnectToDeviceUseCase(
        com.krcast.sender.data.repository.WifiP2pRepositoryImpl(application)
    )
    private val disconnect = DisconnectUseCase(
        com.krcast.sender.data.repository.WifiP2pRepositoryImpl(application)
    )

    private val _capabilities = MutableStateFlow<MirroringCapability?>(null)
    val capabilities: StateFlow<MirroringCapability?> = _capabilities.asStateFlow()

    private val _wifiP2pState = MutableStateFlow<WifiP2pState>(WifiP2pState.Disabled)
    val wifiP2pState: StateFlow<WifiP2pState> = _wifiP2pState.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        loadCapabilities()
        observeWifiP2p.invoke().collect { state ->
            _wifiP2pState.value = state
        }
    }

    fun loadCapabilities() {
        viewModelScope.launch {
            val cap = checkCapabilities.invoke()
            _capabilities.value = cap
        }
    }

    fun startScanning() {
        startDiscovery.invoke()
    }

    fun stopScanning() {
        stopDiscovery.invoke()
    }

    fun connect(device: DiscoveredDevice) {
        connectToDevice.invoke(device)
    }

    fun disconnect() {
        disconnect.invoke()
    }

    fun saveThemePreference(isDark: Boolean) {
        _isDarkTheme.value = isDark
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[preferencesKey<Boolean>("theme_dark")] = isDark
            }
        }
    }
}
