# ADB Setup

Intelli's ADB Connect feature installs/uses `adb` in the selected local terminal environment or on the configured remote server. It connects the phone's ADB daemon to that ADB client. A normal Android application cannot directly act as the host ADB server or access USB debugging without a supported host/companion mechanism.

1. Open **Settings → ADB Connect**.
2. Install or repair `adb` if Intelli reports it missing.
3. On the phone, enable Developer options and Wireless debugging.
4. Pair once using the notification workflow, or use the manual pairing form.
5. Connect; Intelli verifies the device state from `adb devices` before showing it connected.
6. Select a device serial in the Device Access console when more than one device is connected.

USB debugging can authorize a computer running ADB. Intelli does not claim that its Android process itself has unrestricted USB ADB access. Disconnect from ADB Connect to revoke the active connection; revoke the computer/phone authorization in Android Developer options to forget the pairing.
