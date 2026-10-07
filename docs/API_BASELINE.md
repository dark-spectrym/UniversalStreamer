# API baseline — external connections

Single reference for every connection the app makes outside its own process. The
code lives in the platform-agnostic `:baseline` module so it compiles and is unit
tested without an Android SDK.

## 1. Connection surfaces

| # | Surface | Transport | Where |
|---|---|---|---|
| 1 | Backend REST API (`porn-app.com/api/v7`) | Retrofit + OkHttp + Gson | `ApiService`, `StreamerApi` |
| 2 | Direct site scraping | jsoup over HTTP(S) | `sites/SiteConnectionClient` |
| 3 | Media streaming (playback) | ExoPlayer2 + headers from `v7/videoheaders` | app module (port) |
| 4 | Chromecast | GMS Cast framework | app module (port) |
| 5 | Firebase Analytics / Crashlytics | Firebase SDK | optional, app module |
| 6 | AdMob | play-services-ads | optional, app module |

## 2. Backend base config
- Base URL: `https://porn-app.com/api/` (`BaselineConfig.DEFAULT_BASE_URL`, injectable)
- Version prefix: `v7`
- Timeouts: 90 s backend, 60 s scrape (`BaselineConfig`)
- Converter: Gson · Async: Kotlin coroutines (`suspend`)

## 3. Authentication model
Two headers, attached automatically by `AuthInterceptor` (no longer per-method):

- `hash` — **every** request. `base64( RSA/ECB/PKCS1( base64(SHA1(signingCert)) + packageName + unixSeconds ) )` using the server public key in `BaselineConfig.SERVER_PUBLIC_KEY`. Recomputed per request. The backend uses it to confirm a genuine, correctly-signed client.
- `Authorization: Bearer <accessToken>` — authenticated requests only. Endpoints that must not carry it are tagged `@Headers("X-No-Auth: 1")`; the interceptor strips that marker before sending.

Login (`POST v7/login`) sends `UserData { username, password = SHA3-256 hex, android_id }`; the returned `token` is persisted via `CredentialStore`.

## 4. Endpoint map (all 30)
Session: `login`, `device`, `unixTime`*, `checkInfo`* ·
Economy: `tokens` (GET/POST), `coins`, `exchange` ·
Catalogue: `sites`*, `categories`*, `starter`*, `getInfo`* ·
Content: `getData`, `getLink`, `videoheaders`* ·
Favorites: `favorites` (GET/POST/DELETE), `favorites/check`, `favorites/check/id`, `favorites/sites` ·
Playlists: `playlists` (GET/POST), `playlists/favorites`, `playlists/{id}` (PUT/DELETE), `playlists/{id}/favorites` (DELETE) ·
History: `history` (GET/POST/DELETE), `history/sites` ·
Diagnostics: `error`*
(`*` = no bearer token.)

Full signatures: `baseline/src/main/kotlin/com/streamdev/aiostreamer/baseline/ApiService.kt`.

## 5. Direct site scraping
`SiteConnectionClient` centralises outbound site requests that the original app
open-coded with jsoup in many places:
- user agents from `BaselineConfig` (mobile Safari / desktop Chrome),
- optional age-gate cookies (`accessAgeDisclaimerPH`, `cookiesBannerSeen`, `hasVisited`),
- per-site stored cookies via `CookieProvider` (Android: `PrefsCookieProvider`, keyed `<siteTag>Cookie(s)`),
- configurable referrer, timeout, redirect policy.

Returns the raw body or a parsed jsoup `Document`.

## 6. Extending
- New backend endpoint → add to `ApiService`, expose via `StreamerRepository`, add a MockWebServer test.
- Point at staging → pass `baseUrl` to `StreamerApi.create`.
- New site connection → call `SiteConnectionClient.fetch*` with a `SiteRequest`; never open-code jsoup in UI.

## 7. Secrets
No secrets are committed. `SERVER_PUBLIC_KEY` is a public key. The signing-cert
digest is computed at runtime from the app's own signature. Firebase/AdMob keys
live only in a local `google-services.json` (gitignored; see the template).
