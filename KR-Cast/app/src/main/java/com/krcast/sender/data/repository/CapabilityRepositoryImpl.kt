package com.krcast.sender.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.view.WindowManager
import com.krcast.sender.domain.model.MirroringCapability
import com.krcast.sender.domain.repository.CapabilityRepository

class CapabilityRepositoryImpl(private val context: Context) : CapabilityRepository {

    override fun checkMirroringCapabilities(): MirroringCapability {
        val wifiDirectAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)
        val mediaProjectionAvailable = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) != null
        val hardwareEncoderAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_VIDEO_ENCODER)
        val cameraFeature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

        // Honest limitation: Android public SDK does not expose a Miracast Sender API.
        // Wi‑Fi Direct (WifiP2pManager) is NOT Miracast / Wi‑Fi Display.
        // Third-party apps cannot initiate native Miracast transmission on most devices.
        val nativeMiracastAvailable = false

        val limitationMessage = if (!nativeMiracastAvailable) {
            "This device does not expose a public Miracast / Wi‑Fi Display sender API. " +
            "Wi‑Fi Direct peer discovery is available, but actual screen mirroring to a " +
            "Samsung TV requires system-level support that may not be exposed to third‑party apps."
        } else null

        return MirroringCapability(
            wifiDirectAvailable = wifiDirectAvailable,
            mediaProjectionAvailable = mediaProjectionAvailable,
            hardwareEncoderAvailable = hardwareEncoderAvailable,
            nativeMiracastAvailable = nativeMiracastAvailable,
            limitationMessage = limitationMessage
        )
    }
}
