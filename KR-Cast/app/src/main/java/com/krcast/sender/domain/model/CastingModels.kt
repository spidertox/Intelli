package com.krcast.sender.domain.model

/**
 * A discovered Wi‑Fi Direct peer.
 * This does NOT imply Miracast compatibility — it only means the device responded to discovery.
 */
data class DiscoveredDevice(
    val deviceName: String,
    val deviceAddress: String?,
    val connectionState: Int = 0, // WifiP2pDevice constants
    val isGroupOwner: Boolean = false,
    val primaryDeviceType: String? = null
) {
    fun compatibilityStatus(): String {
        // Honest: we cannot verify Miracast compatibility from Wi‑Fi Direct alone
        return "UNVERIFIED"
    }
}

/**
 * Current Wi‑Fi Direct discovery / connection state.
 */
sealed class WifiP2pState {
    object Disabled : WifiP2pState()
    object Enabled : WifiP2pState()
    data class Scanning(val devices: List<DiscoveredDevice> = emptyList()) : WifiP2pState()
    data class Connecting(val device: DiscoveredDevice) : WifiP2pState()
    data class Connected(val device: DiscoveredDevice) : WifiP2pState()
    data class Failed(val message: String) : WifiP2pState()
}

/**
 * Mirroring capability report.
 */
data class MirroringCapability(
    val wifiDirectAvailable: Boolean = false,
    val mediaProjectionAvailable: Boolean = false,
    val hardwareEncoderAvailable: Boolean = false,
    val nativeMiracastAvailable: Boolean = false,
    val limitationMessage: String? = null
)
