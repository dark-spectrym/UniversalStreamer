# UniversalStreamer (AIO Streamer, revived)

Revival of the `com.streamdev.aiostreamer` Android app (AIO Streamer) after the
original source was lost. Recovered by decompiling the shipped APKs — first
v6.4.5 (build 645), then the clean v6.7.1 (build 6719) — and rebuilt as a clean,
modern, multi-module Gradle project centred on a well-defined API baseline for all
connections to the backend and to external sites. The baseline tracks the current
**v9** backend.

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
- **minSdk 24 → targetSdk/compileSdk 35** (Android 7.0 through Android 15; minSdk
  matches the clean v6.7.1 build, upstream targeted a 37-preview).
- Native libs come only from standard deps (Conscrypt/datastore/graphics) and are
  auto-bundled for all 4 ABIs; ABI and density splits are disabled so one universal
  APK installs everywhere.
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
