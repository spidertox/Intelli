# KR Cast — Testing Guide

## Test Devices

| Device | Details |
|--------|---------|
| Phone | Samsung Galaxy F14 5G, Android 15 |
| TV | Samsung UA43UE84AHULXL, Samsung Tizen |

---

## Test Checklist

### 1. App Installation
- [ ] APK installs successfully
- [ ] App icon appears in launcher
- [ ] App opens without crash

### 2. Permission Handling
- [ ] Wi‑Fi permissions requested at runtime
- [ ] NEARBY_WIFI_DEVICES permission handled (Android 13+)
- [ ] Location permission (Android 12 and below, if required)
- [ ] Permissions explained before request

### 3. Wi‑Fi Direct Availability
- [ ] App detects Wi‑Fi Direct support
- [ ] "Not available" shown if unsupported

### 4. Peer Discovery
- [ ] Scan starts when button tapped
- [ ] Discovered peers listed with names
- [ ] Refresh/rescan works
- [ ] Discovery status displayed

### 5. TV Discovery
- [ ] Samsung TV appears in peer list (if discoverable)
- [ ] Device name and address shown
- [ ] Compatibility marked as "Unverified"

### 6. P2P Connection
- [ ] Connection attempt to peer
- [ ] Connection state changes (Connecting → Connected/Failed)
- [ ] Disconnect works

### 7. Native Miracast Capability
- [ ] Diagnostics screen shows Miracast as "Not exposed" (expected)
- [ ] Limitation message displayed honestly

### 8. Actual Screen Transmission
- [ ] **Not implemented** — no custom receiver protocol
- [ ] App does not claim streaming without verification

### 9. TV Image Output
- [ ] Depends on TV's built-in receiver
- [ ] App does not control TV output

### 10. Resolution
- [ ] Diagnostics reports actual capabilities
- [ ] 1080p60 shown only as target, not guaranteed

### 11. Frame Rate
- [ ] Not measurable without active streaming session

### 12. Latency
- [ ] Not measurable without active streaming session

### 13. Audio Behavior
- [ ] Not applicable (sender-only, no streaming pipeline)

### 14. Disconnect and Reconnect
- [ ] Disconnect button works
- [ ] App returns to idle state

### 15. App Background Behavior
- [ ] App survives background/foreground
- [ ] No crash on resume

### 16. Thermal and Battery
- [ ] No excessive heating during discovery
- [ ] Normal battery usage

---

## Distinguishing Test Results

| Result | Meaning |
|--------|---------|
| Discovery successful | Wi‑Fi Direct peer found |
| Wi‑Fi Direct connection successful | P2P group formed |
| Miracast session established | **Not verified** — requires system support |
| Actual screen transmission successful | **Not implemented** |

---

## Expected Limitations (Galaxy F14 5G)

- Wi‑Fi Direct discovery: **Available** (via WifiP2pManager)
- Native Miracast Sender: **Not exposed** by public Android API
- Screen transmission to TV: **Requires TV built-in receiver + system support**
