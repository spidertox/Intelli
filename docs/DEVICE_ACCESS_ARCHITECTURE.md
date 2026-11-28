# Device Access Architecture

Intelli keeps device access inside the existing `:app` module and Kotlin/Compose architecture. Provider code lives under `com.example.gemini.data.deviceaccess`; UI is under `ui.settings`. No provider is a fallback for another provider.

## Providers

- **Normal Terminal** runs through Intelli's existing local environment manager.
- **ADB Shell / Wireless ADB** reuse `AdbConnectManager`. A target serial can be selected when multiple connected devices are present. Wireless ADB is still the terminal's `adb` client connecting to `adbd`; an ordinary Android application does not gain unrestricted host ADB access.
- **Shizuku Shell** uses the official Shizuku API and a user service. It requires a running Shizuku/Sui service and the user's explicit permission. The reported UID distinguishes Android shell (2000) from root (0).
- **Root Shell** starts `su` only after the user confirms the exact command. No root password is stored.
- **Ubuntu PRoot** routes through Intelli's existing Ubuntu PRoot rootfs. It is explicitly identified as PRoot. A separate rooted chroot manager installs the official, hash-pinned Ubuntu Base arm64 image and executes one-shot commands in a private mount namespace when the device supports it.
- **Ubuntu Root Chroot** uses a pinned Ubuntu Base arm64 archive, verifies SHA-256 before extraction, and runs one-shot commands through `su` in a private mount namespace. It requires device/root-manager support and has no interactive PTY.
- **SSH Terminal** requires the existing configured remote server and active session.

`DeviceCommandRequest` identifies the provider, command, device, timeout, approval state, and request ID. `DeviceCommandResult` separates stdout/stderr where the provider supports it. Requests are bounded to 900 seconds and can be cancelled by request ID. Local/ADB/SSH legacy command paths currently merge stdout and stderr; the UI reports stderr only where captured separately.

The provider console is a one-shot command runner. It does not replace Intelli's PTY-backed terminal sessions. Shizuku's command service is not a TTY, so interactive applications remain in the normal terminal.

## AI tools

The existing Android Automation MCP continues to use its explicitly configured Accessibility and ADB tools. The new Root and Shizuku console is not exposed as an unrestricted AI tool. Any future automation adapter must go through the same provider selection, user approval and audit policy.
