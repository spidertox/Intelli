# KR Cast — Compatibility Report

## Verified Capabilities

| Capability | Status | Evidence |
|------------|--------|----------|
| Jetpack Compose UI | Implemented | Material 3, Navigation Compose |
| Wi‑Fi Direct Discovery | Implemented | WifiP2pManager API |
| MediaProjection Check | Implemented | PackageManager feature check |
| Hardware Encoder Check | Implemented | PackageManager feature check |
| GitHub Actions CI | Configured | `.github/workflows/android.yml` |
| Dark/Light Theme | Implemented | Material 3 color schemes |

## Unverified Capabilities

| Capability | Status | Reason |
|------------|--------|--------|
| Native Miracast Sender | Not exposed | Android public SDK has no Miracast Sender API |
| Wi‑Fi Direct to TV streaming | Not tested | Requires actual TV + compatible phone |
| 1080p60 streaming | Target only | No verified streaming pipeline |
| H.264 hardware encoding | Checked via feature flag | Actual encoding pipeline not built |

## Device-Specific Notes

### Samsung Galaxy F14 5G (Android 15)
- Wi‑Fi Direct: Available via `WifiP2pManager`
- Miracast Sender: **Not exposed** to third‑party apps
- Smart View / native mirroring: May be Samsung-specific, not available via public API

### Samsung UA43UE84AHULXL (Tizen)
- Built-in Screen Mirroring: Likely available
- Device Connect Manager: Likely available
- Compatibility with third‑party sender: Untested

## Limitations

1. **No Miracast Sender API** — Android does not provide a public Miracast transmission API for third‑party apps.
2. **Wi‑Fi Direct ≠ Miracast** — `WifiP2pManager` discovers and connects peers but does not stream screen content.
3. **No custom receiver** — Per the project requirements, no TV receiver app is built.
4. **No fake functionality** — The app does not simulate connected or streaming states.

## Recommendations

To achieve actual screen mirroring:
1. Use Samsung Smart View (if available on the device)
2. Use the TV's built-in screen mirroring via system settings
3. Use Chromecast / Google Cast if the TV supports it (requires separate SDK)
