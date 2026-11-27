package com.krcast.sender.data.repository

import android.content.Context
import com.krcast.sender.domain.repository.*
import kotlinx.coroutines.flow.StateFlow

class DataRepository(
    context: Context,
) {
    val wifiP2p: WifiP2pRepository = WifiP2pRepositoryImpl(context)
    val capability: CapabilityRepository = CapabilityRepositoryImpl(context)
    val preference: PreferenceRepository = PreferenceRepositoryImpl(context)
}
