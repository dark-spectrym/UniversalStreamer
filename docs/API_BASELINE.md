# API baseline — external connections (v9)

Single reference for every connection the app makes outside its own process.
Reconstructed from the clean v6.7.1 build (versionCode 6719). The code lives in
the platform-agnostic `:baseline` module so it compiles and is unit tested
without an Android SDK.

## 1. Connection surfaces

| # | Surface | Transport | Where |
|---|---|---|---|
| 1 | Main backend (`porn-app.com/api/v9`) | Retrofit + OkHttp + Gson | `ApiService`, `StreamerApi` |
| 2 | NSFW swipe feed (`nsfwswipe.com/api/`) | Retrofit | `SwipeService` |
| 3 | RedGifs (`api.redgifs.com/v2/`) | Retrofit | `RedgifsService` |
| 4 | Direct site scraping | jsoup over HTTP(S) | `sites/SiteConnectionClient` |
| 5 | Media streaming (playback) | ExoPlayer2 + headers from `v9/videoheaders` | app module (port) |
| 6 | Chromecast / `v9/tv/send` | GMS Cast + REST | app module (port) |
| 7 | Firebase / AdMob | SDKs | optional, app module |

## 2. Main backend config
- Base URL: `https://porn-app.com/api/` (`BaselineConfig.DEFAULT_BASE_URL`, injectable)
- Version prefix: `v9` · Client versionCode: 6719
- Timeouts: 90 s · Converter: Gson · Async: coroutines (`suspend`)

## 3. Authentication model
Two headers, attached automatically by `AuthInterceptor` (not per-method):

- `hash` — **every** request. v9 changed the scheme: it is
  `base64( RSA/ECB/PKCS1( gson.toJson(HashInformation) ) )` with the **new v6.7.1
  server public key** (`BaselineConfig.SERVER_PUBLIC_KEY`). `HashInformation`
  carries `{ id (android_id), isTV, loginStatus, packageName, signatures[], time,
  version }`, where `signatures` are **SHA-256** cert digests, base64. (v7 used a
  SHA-1 string `base64(SHA1(cert))+pkg+time` — fully replaced.)
- `Authorization: Bearer <accessToken>` — authenticated requests only. No-bearer
  endpoints are tagged `@Headers("X-No-Auth: 1")`; the interceptor strips the marker.

Login (`POST v9/login`) sends `UserData { username, password = SHA3-256 hex, android_id }`.
The returned snapshot (token + pro/unixtime/status/user_id) is persisted and fed
back into every subsequent request hash.

## 4. v9 endpoint map (RESTful redesign)
Session: `login`, `device`, `unixTime`*, `checkInfo`* ·
Catalogue: `sites`*, `categories`*, `starter`*, `sites/{tag}/info`* ·
Content: `sites/{tag}/data`, `/related`, `/link`, `/stream`, `/tags`, `/extra`,
`videoheaders`*, `video/{id}/info` (GET + POST*), `porndb` ·
Favorites: `favorites` (GET/POST/DELETE), `favorites/{video_id}`, `favorites/sites` ·
Playlists: `playlists` (GET/POST), `playlists/favorites`,
`playlists/{id}` (PUT/PATCH/DELETE), `playlists/{id}/favorites` (DELETE) ·
History: `history` (GET/POST/DELETE), `history/sites` ·
Coins: `coins/check`, `coins/exchange`, `coins/id` ·
Cast/diag: `tv/send`, `error`, `errors` ·
Utility (no version, no bearer): `update`, `addDownloadCount`, streaming `@Url` download.
(`*` = no bearer token.)

Changes vs v7: flat verbs (`getData`, `getInfo`, `getLink`, `getStream`, `getRelatedVideos`)
became `sites/{sitetag}/{data|info|link|stream|related|tags|extra}`; `tokens` became
`coins/*`; `porndb` and `tv/send` are new. Full signatures: `baseline/.../ApiService.kt`.

## 5. Auxiliary services
- `SwipeService` (`nsfwswipe.com/api/`): `getCategories`, `getVideos/{id}/{orientation}`,
  `like/{id}`, `increase/{id}`. No app auth.
- `RedgifsService` (`api.redgifs.com/v2/`): `auth/temporary` then an `@Url` gif fetch
  with the temporary bearer. No app auth.

## 6. Direct site scraping
`SiteConnectionClient` centralises outbound site requests (UA, age-gate cookies,
per-site stored cookies via `CookieProvider`, referrer/timeout/redirects). Returns
raw body or a parsed jsoup `Document`.

## 7. Secrets
No secrets committed. `SERVER_PUBLIC_KEY` is a public key (verified: 550-byte DER
SPKI). The signing-cert digest is computed at runtime from the app's own signature.
Firebase/AdMob keys live only in a local, gitignored `google-services.json`.
