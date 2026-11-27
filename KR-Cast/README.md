# KR Cast — Professional Android Sender App

KR Cast is a premium Android screen-casting **sender** application built with Jetpack Compose and Material 3. It discovers nearby Wi‑Fi Direct peers and provides a professional UI for connecting to Samsung TVs with built-in Screen Mirroring.

**App name:** KR Cast  
**Package:** `com.krcast.sender`  
**Tagline:** _Your Screen. Your Big Display._

---

## ⚠️ Important: Technically Honest

KR Cast is a **sender app only**. It does **not** create a TV receiver app. Your Samsung TV must already have a built-in receiver (Screen Mirroring / Device Connect Manager).

**Android limitation:** The public Android SDK does **not** expose a Miracast / Wi‑Fi Display Sender API. `WifiP2pManager` provides Wi‑Fi Direct peer discovery and group formation — it is **not** Miracast. On most devices (including Samsung Galaxy F14 5G), third‑party apps cannot initiate native Miracast transmission to a TV.

KR Cast honestly reports this limitation instead of faking a connection.

---

## Features

- Material 3 dark-first UI with smooth animations
- Splash screen, Home dashboard, Device Discovery, Connection Guide, Casting Dashboard, Settings, Diagnostics
- Wi‑Fi Direct peer discovery using `WifiP2pManager`
- Mirroring capability diagnostics (Wi‑Fi Direct, MediaProjection, hardware encoder)
- No fake connection states, no fake streaming indicators
- No analytics, no advertising SDKs
- GitHub Actions CI for APK builds

---

## Screens

| Screen | Purpose |
|--------|---------|
| Splash | Animated brand intro |
| Home | Status overview, "Find My TV" CTA |
| Discovery | Wi‑Fi Direct peer scan |
| Connection Guide | Samsung TV setup steps |
| Casting | Active session dashboard |
| Settings | Theme, about, privacy |
| Diagnostics | Capability report with copy |

---

## Architecture

Clean architecture with:
- **UI:** Jetpack Compose + Material 3 + Navigation Compose
- **State:** ViewModel + StateFlow
- **Data:** Repository pattern + DataStore preferences
- **Network:** Wi‑Fi Direct (documented Android APIs only)

```
app/src/main/java/com/krcast/sender/
├── MainActivity.kt
├── KRApplication.kt
├── navigation/AppNavigator.kt
├── ui/theme/
├── ui/components/
├── ui/screens/
├── data/repository/
├── domain/model/
├── domain/repository/
└── domain/usecase/
```

---

## Requirements

- Android 8.0 (API 26) or higher
- Target: Android 15 (API 35)
- Wi‑Fi Direct capable hardware
- Samsung TV with Screen Mirroring / Device Connect Manager

---

## Development Setup

### Prerequisites
- JDK 17
- Android SDK 35
- Gradle 8.5+

### Clone
```bash
git clone <your-repo-url>
cd KR-Cast
```

### Generate Gradle Wrapper
```bash
gradle wrapper --gradle-version 8.5
```

### Build Debug APK
```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

---

## GitHub Actions

Push to `main` or create a tag (`v1.0`) to trigger CI. The workflow:
1. Builds debug APK
2. Runs lint and unit tests
3. Creates a GitHub Release (on tag push)
4. Uploads APK as artifact

Download APK from: **Actions → Build → Artifacts** or **Releases**.

---

## Samsung TV Testing Guide

1. Turn on Samsung TV (UA43UE84AHULXL).
2. Open **Screen Mirroring** or **Device Connect Manager** on TV.
3. Enable receiving mode.
4. Install KR Cast APK on phone.
5. Open app → **Find My TV** → Scan.
6. If a peer is found, follow the Connection Guide steps.
7. Note: Discovery does not guarantee Miracast streaming.

See `TESTING.md` for full checklist.

---

## Compatibility

See `COMPATIBILITY.md` for verified and unverified capabilities.

---

## Privacy

KR Cast does not:
- Collect personal data
- Upload screen content
- Store Wi‑Fi passwords
- Include analytics or ads

---

## License

MIT License
