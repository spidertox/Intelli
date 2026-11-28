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

    @Test fun ubuntuBackupIdsCannotSelectPaths() {
        assertEquals(true, RootedUbuntuManager.DigestPolicy.isValidBackupId("1735689600000-a1b2c3d4"))
        listOf("../rootfs", "ubuntu-rootfs-1735689600000-a1b2c3d4", "1735689600000-a1b2c3d4/../../x", "1735689600000-A1B2C3D4")
            .forEach { id -> assertEquals(false, RootedUbuntuManager.DigestPolicy.isValidBackupId(id)) }
    }

    @Test fun ubuntuBackupDigestMustBeAFullLowercaseSha256() {
        assertEquals(true, RootedUbuntuManager.DigestPolicy.isSha256("a".repeat(64)))
        assertEquals(false, RootedUbuntuManager.DigestPolicy.isSha256("a".repeat(63)))
        assertEquals(false, RootedUbuntuManager.DigestPolicy.isSha256("A".repeat(64)))
        assertEquals(false, RootedUbuntuManager.DigestPolicy.isSha256("z".repeat(64)))
    }
}
