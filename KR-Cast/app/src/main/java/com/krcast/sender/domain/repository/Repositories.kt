package com.krcast.sender.domain.repository

import com.krcast.sender.domain.model.DiscoveredDevice
import com.krcast.sender.domain.model.MirroringCapability
import com.krcast.sender.domain.model.WifiP2pState
import kotlinx.coroutines.flow.Flow

interface WifiP2pRepository {
    fun isWifiDirectAvailable(): Boolean
    fun observeWifiP2pState(): Flow<WifiP2pState>
    fun startDiscovery()
    fun stopDiscovery()
    fun connectToDevice(device: DiscoveredDevice)
    fun disconnect()
}

interface CapabilityRepository {
    fun checkMirroringCapabilities(): MirroringCapability
}

interface PreferenceRepository {
    val isDarkTheme: Boolean
    fun saveThemePreference(isDark: Boolean)
    val isAutoScan: Boolean
    fun saveAutoScanPreference(autoScan: Boolean)
}
