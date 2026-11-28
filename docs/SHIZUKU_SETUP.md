# Shizuku Setup

Intelli integrates the official Shizuku API (`dev.rikka.shizuku:api` and `provider`, pinned to 12.2.0). Shizuku itself is a separate application and is not bundled by Intelli.

1. Install Shizuku from its [official download page](https://shizuku.rikka.app/download/).
2. Start Shizuku. On Android 11+, follow Shizuku's Wireless debugging instructions. On older supported Android versions, a computer running ADB may be required. Rooted devices may start it through their supported root manager.
3. In Intelli, open **Settings → Device Access → Shizuku Shell** and request permission.
4. Accept the permission prompt in Shizuku. Intelli reports the binder and user-service connection separately.

If Shizuku stops, its binder dies, or permission is revoked, execution is unavailable until the user restarts Shizuku or grants permission again. Shizuku started through ADB normally needs to be started again after reboot. Shell UID (2000) is not root UID (0); Android and OEM restrictions still apply. The Shizuku user service executes non-interactive commands and does not provide a PTY.

Official docs: [Shizuku API](https://github.com/RikkaApps/Shizuku-API), [Shizuku](https://github.com/RikkaApps/Shizuku).
