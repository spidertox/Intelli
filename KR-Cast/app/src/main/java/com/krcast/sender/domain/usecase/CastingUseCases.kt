package com.krcast.sender.domain.usecase

import com.krcast.sender.domain.model.DiscoveredDevice
import com.krcast.sender.domain.model.MirroringCapability
import com.krcast.sender.domain.model.WifiP2pState
import com.krcast.sender.domain.repository.WifiP2pRepository
import com.krcast.sender.domain.repository.CapabilityRepository
import kotlinx.coroutines.flow.Flow

class CheckMirroringCapabilitiesUseCase(private val capabilityRepository: CapabilityRepository) {
    fun invoke(): MirroringCapability = capabilityRepository.checkMirroringCapabilities()
}

class ObserveWifiP2pStateUseCase(private val wifiP2pRepository: WifiP2pRepository): Flow<WifiP2pState> {
    override operator fun subscribe(coroutineScope: kotlinx.coroutines.CoroutineScope, sink: kotlinx.coroutines.flow.FlowCollector<WifiP2pState>) =
        wifiP2pRepository.observeWifiP2pState().subscribe(coroutineScope, sink)
}

class StartDiscoveryUseCase(private val wifiP2pRepository: WifiP2pRepository) {
    fun invoke() = wifiP2pRepository.startDiscovery()
}

class StopDiscoveryUseCase(private val wifiP2pRepository: WifiP2pRepository) {
    fun invoke() = wifiP2pRepository.stopDiscovery()
}

class ConnectToDeviceUseCase(private val wifiP2pRepository: WifiP2pRepository) {
    fun invoke(device: DiscoveredDevice) = wifiP2pRepository.connectToDevice(device)
}

class DisconnectUseCase(private val wifiP2pRepository: WifiP2pRepository) {
    fun invoke() = wifiP2pRepository.disconnect()
}
