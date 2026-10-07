# UniversalStreamer (AIO Streamer, revived)

Revival of the `com.streamdev.aiostreamer` Android app (shipped as AIO Streamer
v6.4.5, build 645) after the original source was lost. The codebase was recovered
by decompiling the shipped APK and is being rebuilt as a clean, modern,
multi-module Gradle project centred on a well-defined API baseline for all
connections to the backend and to external sites.

## Status
- ✅ **API baseline** — reconstructed, modernised (coroutines), compiles and unit-tested (`:baseline`, 7 tests green).
- ✅ **Project scaffold** — AGP 8.7 + Kotlin 2.0 + version catalog; Android `:app` wired to the baseline with a shell launcher.
- 🟡 **Feature port** — the full feature surface is inventoried in [`docs/FEATURES.md`](docs/FEATURES.md) and ported onto the baseline incrementally.

## Layout
- `baseline/` — platform-agnostic networking / API baseline (no Android deps).
- `app/` — Android application (depends on `:baseline`).
- `docs/` — [FEATURES](docs/FEATURES.md) · [API_BASELINE](docs/API_BASELINE.md) · [ARCHITECTURE](docs/ARCHITECTURE.md)

## Universal / modern Android
Built as one sideloadable universal APK that targets the current platform while
keeping the widest device reach:
- **minSdk 21 → targetSdk/compileSdk 35** (Android 5.0 through Android 15).
- **No native libraries** (pure Dalvik), so the single APK is ABI-universal; ABI
  and density splits are disabled explicitly.
- **All screen classes** (phone, tablet, foldable, Chromebook) plus **Android TV**
  (leanback is optional, touchscreen not required), with `resizeableActivity`.
- **Edge-to-edge** window-inset handling for Android 15's enforced layout.
- **Modern permissions**: runtime notifications (13+), granular media read (13+),
  typed foreground services (14+), predictive back (`enableOnBackInvokedCallback`).
- **Always-installable release**: `assembleRelease` signs with your key
  (`keystore.properties`) if present, else falls back to the debug key.

## Build
```bash
# API baseline: compiles + tests without an Android SDK (CI-friendly)
SKIP_APP=1 ./gradlew :baseline:test

# Full universal APK: requires an Android SDK (ANDROID_HOME or local.properties
# sdk.dir) and access to Google's Maven repository
./gradlew :app:assembleRelease   # -> app/build/outputs/apk/release/app-release.apk
```

## Notes
- The recovered APK was a modified "Premium" build. The revival removes the
  tamper/crack scaffolding and restores correct membership handling — see
  `docs/FEATURES.md` §8.
- No secrets are committed. Firebase config is a local, gitignored file; drop a
  real `google-services.json` in `app/` (template provided) to re-enable Firebase.
- This app streams adult content from third-party sites; ship and use it only
  where that is lawful, and only against sites/accounts you are authorised to use.
