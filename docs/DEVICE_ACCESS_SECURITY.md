# Device Access Security Review

## Access boundaries

- Provider choice is explicit. A failed provider never falls through to another provider.
- Root commands require a UI confirmation of the exact command and then the `su` manager's authorization. No root credential is stored.
- Shizuku requires its own user-granted permission and a live binder. Its UID is shown because shell UID and root UID have different authority.
- ADB commands require a verified connected serial. Wireless debugging authorization remains controlled by Android Settings.
- AI Android Automation retains its existing Accessibility/ADB boundary. The new console is not exposed as an unrestricted privileged AI tool.
- Provider command text is bounded and timeout-limited. Provider-specific process arguments are passed as vectors where available. Shell command text is inherently executable and must be treated as user code.

## Existing application permissions

The app manifest already requests broad permissions for its existing storage, package visibility, microphone, overlay, installation, notifications, and background features. Device Access must not add permissions without a concrete feature need. Accessibility control is enabled by the user in Android Settings.

## Signing and release

Release signing credentials must be supplied from CI secrets or a local, untracked keystore. The repository must never contain a production keystore or passwords. Release jobs must fail when the original signing material is unavailable; generating an ephemeral signing key would break app update identity.

## Limits

Android/OEM restrictions affect ADB, Shizuku and root independently. PRoot is not root. Command output can contain secrets; do not share logs without reviewing them. No silent background privileged execution is supported.
