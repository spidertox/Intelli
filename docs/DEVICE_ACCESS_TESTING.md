# Device Access Testing

## Local automated coverage

Run the existing unit-test suite with `./gradlew :app:testStandardDebugUnitTest`. Current device-access unit tests cover command validation, timeout bounds, privileged approval, output-field separation, and Ubuntu backup identifier/digest validation. Provider process lifecycles, Shizuku permission/binder transitions, Root authorization, backup/restore on a rooted device, and ADB disconnection still need device or injected-process tests. Mock results must remain marked as test-only.

## Device matrix

Exercise Android 10, 12, 13, 14, and 15+ on at least one non-root device and one supported rooted device where available:

- ADB pairing and reconnect after Wi-Fi/Wireless debugging changes.
- Shizuku missing, stopped, permission denied/granted/revoked, binder death, and user-service disconnect.
- Root unavailable, authorization denied, command timeout, and cancellation.
- PRoot Ubuntu remains distinct from any future root chroot.
- Provider switching while a command is running does not redirect or corrupt that command.

No device or root hardware test should be represented as passing unless it was actually run. The current repository does not provide physical-device test hardware in CI.
