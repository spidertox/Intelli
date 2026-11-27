package com.krcast.sender.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import com.krcast.sender.domain.model.DiscoveredDevice
import com.krcast.sender.domain.model.WifiP2pState
import com.krcast.sender.domain.repository.WifiP2pRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WifiP2pRepositoryImpl(private val context: Context) : WifiP2pManager.ActionListener, WifiP2pRepository {

    private val wifiP2pManager: WifiP2pManager? =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    private val channel: WifiP2pManager.Channel? by lazy {
        wifiP2pManager?.initialize(context.mainLooper, null, null)
    }

    private val _state = MutableStateFlow<WifiP2pState>(WifiP2pState.Disabled)
    override val state: StateFlow<WifiP2pState> = _state.asStateFlow()

    private val receiver = WifiP2pBroadcastReceiver(this)

    override fun isWifiDirectAvailable(): Boolean = wifiP2pManager != null

    override fun observeWifiP2pState(): kotlinx.coroutines.flow.Flow<WifiP2pState> {
        context.registerReceiver(receiver, IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        })
        return state
    }

    override fun startDiscovery() {
        channel?.let { ch ->
            wifiP2pManager?.discoverPeers(ch, this)
            _state.value = WifiP2pState.Scanning()
        } ?: run {
            _state.value = WifiP2pState.Failed("Wi‑Fi Direct not available on this device")
        }
    }

    override fun stopDiscovery() {
        _state.value = WifiP2pState.Disabled
    }

    override fun connectToDevice(device: DiscoveredDevice) {
        device.deviceAddress?.let { address ->
            val config = WifiP2pConfig().apply {
                this.deviceAddress = address
            }
            wifiP2pManager?.connect(channel, config, this)
        }
    }

    override fun disconnect() {
        wifiP2pManager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _state.value = WifiP2pState.Disabled
            }
            override fun onFailure(reason: Int) {
                _state.value = WifiP2pState.Failed("Disconnect failed: $reason")
            }
        })
    }

    fun onStateChanged(available: Boolean) {
        _state.value = if (available) WifiP2pState.Enabled else WifiP2pState.Disabled
    }

    fun onPeersChanged(peers: WifiP2pDeviceList?) {
        val devices = peers?.deviceList?.map { d ->
            DiscoveredDevice(
                deviceName = d.deviceName,
                deviceAddress = d.deviceAddress,
                connectionState = d.status,
                isGroupOwner = d.isGroupOwner,
                primaryDeviceType = d.primaryDeviceType
            )
        } ?: emptyList()
        _state.value = WifiP2pState.Scanning(devices)
    }

    fun onConnectionChanged(info: WifiP2pManager.ConnectionInfo?, device: WifiP2pDevice?) {
        if (info?.groupFormed == true) {
            device?.let { d ->
                _state.value = WifiP2pState.Connected(
                    DiscoveredDevice(
                        d.deviceName,
                        d.deviceAddress,
                        d.status,
                        d.isGroupOwner,
                        d.primaryDeviceType
                    )
                )
            }
        } else {
            _state.value = WifiP2pState.Disabled
        }
    }

    fun onThisDeviceChanged(device: WifiP2pDevice?) {
    }

    override fun onSuccess() {
    }

    override fun onFailure(reasonCode: Int) {
        _state.value = WifiP2pState.Failed("Discovery failed: $reasonCode")
    }
}

private class WifiP2pBroadcastReceiver(private val repository: WifiP2pRepositoryImpl) :
    BroadcastReceiver() {

    override fun onReceive(context: android.content.Context, intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val available = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1) == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                repository.onStateChanged(available)
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                repository.channel?.let { ch ->
                    repository.wifiP2pManager?.requestPeers(ch) { peers ->
                        repository.onPeersChanged(peers)
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val info = intent.getParcelableExtra(WifiP2pManager.EXTRA_CONNECTION_INFO) as? WifiP2pManager.ConnectionInfo
                val device = intent.getParcelableExtra(WifiP2pManager.EXTRA_DEVICE) as? WifiP2pDevice
                repository.onConnectionChanged(info, device)
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val device = intent.getParcelableExtra(WifiP2pManager.EXTRA_THIS_DEVICE) as? WifiP2pDevice
                repository.onThisDeviceChanged(device)
            }
        }
    }
}
