package com.example.gemini.data.deviceaccess

import java.util.UUID

enum class DeviceAccessProvider(val label: String) {
    NORMAL("Normal Terminal"),
    ADB("ADB Shell"),
    WIRELESS_ADB("Wireless ADB"),
    SHIZUKU("Shizuku Shell"),
    ROOT("Root Shell"),
    UBUNTU("Ubuntu PRoot"),
    UBUNTU_CHROOT("Ubuntu Root Chroot"),
    SSH("SSH Terminal")
}

enum class ProviderAvailability {
    AVAILABLE, DISCONNECTED, PERMISSION_REQUIRED, NOT_INSTALLED, NOT_SUPPORTED, ERROR
}

data class ProviderStatus(
    val provider: DeviceAccessProvider,
    val availability: ProviderAvailability,
    val detail: String = ""
)

data class DeviceCommandRequest(
    val provider: DeviceAccessProvider,
    /** The exact shell text entered/approved by the user. Providers never change it or fall back. */
    val command: String,
    val targetDeviceId: String? = null,
    val timeoutSeconds: Long = 60,
    val privilegedApproved: Boolean = false,
    val id: String = UUID.randomUUID().toString()
)

data class DeviceCommandResult(
    val requestId: String,
    val provider: DeviceAccessProvider,
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMs: Long,
    val timedOut: Boolean = false,
    val cancelled: Boolean = false
)

class DeviceAccessException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Pure request policy used by the manager and unit tests. */
object DeviceAccessRequestPolicy {
    fun validate(request: DeviceCommandRequest) {
        require(request.command.isNotBlank()) { "Command cannot be empty." }
        require(request.command.length <= 32_768) { "Command is too long." }
        require(request.timeoutSeconds in 1..900) { "Timeout must be between 1 and 900 seconds." }
        if ((request.provider == DeviceAccessProvider.ROOT || request.provider == DeviceAccessProvider.UBUNTU_CHROOT) && !request.privilegedApproved) {
            throw SecurityException("Privileged execution requires explicit confirmation of the exact command.")
        }
    }
}
