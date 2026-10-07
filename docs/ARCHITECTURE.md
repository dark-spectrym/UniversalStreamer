# Architecture

## Why the rewrite
The source was lost; the only surviving artifact was a shipped v6.4.5 "Premium"
APK. It was recovered by decompilation (jadx) and is being rebuilt as a clean,
modern Gradle project rather than patched decompiler output. The original was a
single-module app that mixed UI, RxJava networking, scraping and crack scaffolding;
the revival separates concerns and restores stock behaviour.

## Module layout
```
UniversalStreamer/
├─ baseline/   Pure Kotlin/JVM. All outside-world connections: Retrofit API,
│              DTOs, auth/hash, repository, site-scraping client. No Android deps
│              → compiles + unit-tests without an SDK. This is the "API baseline".
└─ app/        Android application. Depends on :baseline and supplies the platform
               pieces (SharedPreferences, signing-cert hash, cookie store) plus UI.
```

The split is the core architectural improvement: the entire external-connection
surface is isolated, testable, and reusable (e.g. by a future TV-only or desktop
client), and cannot accidentally depend on Android UI.

## Layers
- **baseline/security** — `PasswordHasher` (SHA3), `HashSigner` + `RsaHashSigner` (per-request integrity hash). Android supplies the signing-cert digest via `AndroidHashSigner`.
- **baseline/net** — `AuthInterceptor` (injects `hash` + bearer), `CredentialStore`.
- **baseline/model** — Gson DTOs, wire-compatible with the existing backend.
- **baseline (`ApiService`, `StreamerApi`, `StreamerRepository`)** — typed endpoints, client factory, coroutine repository returning `Result`.
- **baseline/sites** — `SiteConnectionClient`, the one place direct jsoup site requests happen.
- **app/core** — `SharedPref`, `PrefsCredentialStore`, `PrefsCookieProvider`, `AndroidHashSigner`.
- **app/app** — `StreamerApp` (Application) + `Graph` (manual DI).
- **app/ui** — `MainActivity` shell today; feature screens ported per `docs/FEATURES.md`.

## Key technology changes vs v6.4.5
| Original | Revival | Reason |
|---|---|---|
| RxJava3 `Observable` + `Observer` at every call site | Kotlin coroutines `suspend` + `Result` | less boilerplate, structured concurrency |
| `RetrofitClient` singleton, headers per method | `StreamerApi` factory + `AuthInterceptor` | one auth path, injectable base URL, testable |
| Scattered jsoup calls, duplicated UA/cookie code | `SiteConnectionClient` | single audited egress point |
| Obfuscated `defpackage` + signature-killer + hidden-API bypass | removed | crack scaffolding, not a feature |
| Hard-coded forced-PRO patch | real server membership value | correctness |
| Groovy/implicit build | AGP 8.7 + Kotlin 2.0 + version catalog | reproducible, current |

## Build & verify
- `SKIP_APP=1 ./gradlew :baseline:test` — builds and tests the API baseline. Needs only mavenCentral; **runs in CI without an Android SDK**.
- `./gradlew :app:assembleDebug` — builds the APK. Needs an Android SDK (`local.properties` `sdk.dir`, or `ANDROID_HOME`) and access to Google's Maven repository.

## Port strategy
Each ⬜ feature in `docs/FEATURES.md` is ported onto the baseline one screen/subsystem
at a time: build the UI against `StreamerRepository` / `SiteConnectionClient`, delete
the corresponding decompiled class, keep the wire format unchanged so the live backend
and existing installs keep working.
