package com.example.gemini.data.deviceaccess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DeviceAccessRequestPolicyTest {
    @Test fun acceptsNormalCommandWithinTimeoutBounds() {
        val request = DeviceCommandRequest(DeviceAccessProvider.NORMAL, "id", timeoutSeconds = 20)
        DeviceAccessRequestPolicy.validate(request)
        assertEquals(DeviceAccessProvider.NORMAL, request.provider)
    }

    @Test fun rejectsBlankAndOversizedCommands() {
        assertThrows(IllegalArgumentException::class.java) {
            DeviceAccessRequestPolicy.validate(DeviceCommandRequest(DeviceAccessProvider.NORMAL, "  "))
        }
        assertThrows(IllegalArgumentException::class.java) {
            DeviceAccessRequestPolicy.validate(DeviceCommandRequest(DeviceAccessProvider.NORMAL, "x".repeat(32_769)))
        }
    }

    @Test fun rejectsInvalidTimeout() {
        assertThrows(IllegalArgumentException::class.java) {
            DeviceAccessRequestPolicy.validate(DeviceCommandRequest(DeviceAccessProvider.NORMAL, "id", timeoutSeconds = 0))
        }
        assertThrows(IllegalArgumentException::class.java) {
            DeviceAccessRequestPolicy.validate(DeviceCommandRequest(DeviceAccessProvider.NORMAL, "id", timeoutSeconds = 901))
        }
    }

    @Test fun rootAndChrootRequireExplicitApproval() {
        listOf(DeviceAccessProvider.ROOT, DeviceAccessProvider.UBUNTU_CHROOT).forEach { provider ->
            assertThrows(SecurityException::class.java) {
                DeviceAccessRequestPolicy.validate(DeviceCommandRequest(provider, "id"))
            }
            DeviceAccessRequestPolicy.validate(DeviceCommandRequest(provider, "id", privilegedApproved = true))
        }
    }

    @Test fun shellOutputIsKeptInSeparateFields() {
        val result = DeviceCommandResult("request", DeviceAccessProvider.SHIZUKU, "out", "err", 9, 12)
        assertEquals("out", result.stdout)
        assertEquals("err", result.stderr)
        assertEquals(9, result.exitCode)
    }
}
