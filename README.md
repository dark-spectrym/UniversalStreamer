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

## Build
```bash
# API baseline: compiles + tests without an Android SDK (CI-friendly)
SKIP_APP=1 ./gradlew :baseline:test

# Full app: requires an Android SDK (ANDROID_HOME or local.properties sdk.dir)
# and access to Google's Maven repository
./gradlew :app:assembleDebug
```

## Notes
- The recovered APK was a modified "Premium" build. The revival removes the
  tamper/crack scaffolding and restores correct membership handling — see
  `docs/FEATURES.md` §8.
- No secrets are committed. Firebase config is a local, gitignored file; drop a
  real `google-services.json` in `app/` (template provided) to re-enable Firebase.
- This app streams adult content from third-party sites; ship and use it only
  where that is lawful, and only against sites/accounts you are authorised to use.
