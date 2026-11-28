# Wireless ADB Setup

Intelli's existing ADB Connect screen supports Android Wireless debugging (Android 11+), mDNS pairing/connect port discovery, pairing-code entry through a notification, manual pairing, reconnect, and disconnect.

1. Open **Settings → ADB Connect** and install/repair `adb`.
2. Enable **Developer options → Wireless debugging** and allow notifications for Intelli.
3. Tap **Start pairing**, then choose **Pair device with pairing code** in Android Settings.
4. Enter the six-digit code in Intelli's notification while the pairing dialog remains open.
5. Connect and confirm the device appears as `device` in the verified device list.

Pairing and connection ports are different and can change after Wireless debugging restarts. Intelli discovers the advertised ports where possible; it must not mark a connection successful based only on a pairing response. On Android 10 or older, use the documented computer-assisted ADB route; on-device Wireless debugging is not available there.
