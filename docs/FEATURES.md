# Feature inventory (deconstructed from v6.4.5, build 645)

This is the complete catalogue of features recovered by decompiling the shipped
`AIO_Streamer_v6.4.5` APK (package `com.streamdev.aiostreamer`, 197 app classes,
~46k LOC across 9 dex files). It is the contract for the rewrite: every item here
must survive the port. Status reflects what the revived codebase implements today.

Legend: ✅ reconstructed · 🟡 scaffolded/wired to baseline · ⬜ documented, not yet ported

## 1. Backend / networking
| Feature | Original source | Status |
|---|---|---|
| REST client to `porn-app.com/api/v7` (30 endpoints) | `api/ApiService`, `api/RetrofitClient` | ✅ `baseline/ApiService.kt`, `StreamerApi.kt` |
| Per-request integrity `hash` header (RSA-encrypted signing-cert digest + pkg + time) | `helper/HelperClass.generateHash` | ✅ `baseline/security/RsaHashSigner` + `core/security/AndroidHashSigner` |
| Bearer-token auth, token persistence | scattered `@Header("Authorization")` + `SharedPref` | ✅ `baseline/net/AuthInterceptor`, `PrefsCredentialStore` |
| SHA3-256 password hashing (password never sent in clear) | `datatypes/login/UserData.generateSHA` | ✅ `baseline/security/PasswordHasher` |
| Direct site scraping (jsoup) with per-site UA / age-gate / premium cookies | `methods/GetDataRows`, `methods/GetStream` | ✅ baseline `sites/SiteConnectionClient` + `PrefsCookieProvider` |
| Server-side stream resolution (`getStream`, `getRelatedVideos`) | `methods/GetStream` | 🟡 endpoint wired; per-site extraction ported incrementally |
| High-level repository (coroutines + `Result`) replacing RxJava observers | n/a (new) | ✅ `baseline/StreamerRepository` |
| Crash/scrape error reporting (`v7/error`) | `api/ErrorMethods`, `datatypes/GenericError` | ✅ model + endpoint |

## 2. Accounts, PRO membership & token economy
| Feature | Original source | Status |
|---|---|---|
| Login / signup / password reset flow | `helper/LoginHelper` | 🟡 `repository.login` ready; dialogs ⬜ |
| PRO membership state (expiry unix time) | `datatypes/LoginStatus` | ✅ `LoginStatus.isPro()` (crack patch removed, see §8) |
| Daily token claim, coin exchange, HWID coins | `standardUI/TokensFragment`, endpoints | 🟡 endpoints wired; UI ⬜ |
| Device registration (`v7/device`) | endpoint | ✅ endpoint |

## 3. Content browsing UI
| Feature | Original source | Status |
|---|---|---|
| Navigation drawer shell | `main/NavDrawer`, `NavDrawer1`, `main/Main` | ⬜ |
| Site grid / list, favorites-site ordering | `adapter/SitesAdapter`, `SITESFragmentNew`, `PAYSITESFragmentNew` | ⬜ |
| Standard site video grid + paging + filters | `ui/StandardSite`, `methods/GetDataRows`, `filters/*` | ⬜ (filter model ✅) |
| Category & search filters per site | `helper/SetupFilters` (1.9k LOC), `filters/*Filter` | ⬜ (recipe model ✅ `SiteInformation`) |
| Global search across sites | `standardUI/GLOBALSEARCHFragment`, `GlobalSearchAuthFragment` | ⬜ |
| Related videos | `standardUI/RelatedVideosFragment` | ⬜ |
| "NSFW swipe" (TikTok-style) feed | `ui/swipe/Swipe2Fragment`, `NSFWSWIPECOMFragment`, `VideoAdapter` | ⬜ |
| Image slider / promo offers | `adapter/SliderAdapter`, `ui/misc/PROMOOFFERFragment` | ⬜ |

## 4. Premium paysite integrations
| Feature | Original source | Status |
|---|---|---|
| WebView login capture for ~17 paysite networks (cookies stored per site) | `ui/paysites/*Fragment`, `tv/login/*Login` | ⬜ (cookie plumbing ✅) |
| Premium cookie reuse during scraping | `methods/GetDataRows`, `helper/CookieDeleter` | ✅ `SiteConnectionClient` |
| Per-site cookie deletion / logout | `helper/CookieDeleter` | ⬜ |

## 5. Playback
| Feature | Original source | Status |
|---|---|---|
| ExoPlayer2 video player (HLS/DASH/progressive) | `videoplayer/MergePlayer`, `VideoFragment` | ⬜ (ExoPlayer2 deps declared) |
| Track selection dialog (quality/audio/subs) | `videoplayer/TrackSelectionDialog` | ⬜ |
| Picture-in-picture | manifest `supportsPictureInPicture` | ⬜ |
| VR / 360 player | `videoplayer/VideoPlayerVR` | ⬜ |
| Chromecast (sender + expanded controls) | `utils/CastOptionsProvider`, `ExpandedControlsActivity` | ⬜ (cast deps declared) |
| Playback headers/cookies per stream | `datatypes/VideoHeaders` | ✅ model + endpoint |
| Video cache | `helper/VideoCache`, `utils/CacheDataSourceFactory` | ⬜ |

## 6. Downloads
| Feature | Original source | Status |
|---|---|---|
| Video downloader + progress tracking | `helper/DownloaderClass`, `adapter/ProgressTracker` | ⬜ |
| Download manager UI / queue | `standardUI/DownloadFragment`, `DownloadManagerFragment`, adapters | ⬜ |

## 7. Cloud sync, lock & misc
| Feature | Original source | Status |
|---|---|---|
| Cloud favorites + playlists | `standardUI/cloud/CloudFavoritesFragment`, `api/FavoritesMethods` | ✅ endpoints/models; UI ⬜ |
| Watch history (cloud) | `standardUI/cloud/CloudHistoryFragment`, `api/HistoryMethods` | ✅ endpoints/models; UI ⬜ |
| App lock: PIN + fingerprint/biometric | `lock/MainActivity`, `PinLock2`, `FingerPrint` | ⬜ (biometric dep declared) |
| Preferences screen | `main/PREFFragment` (868 LOC), `res/xml/preferences.xml` | ⬜ |
| In-app update check / APK install | `utils/UpdateCheck`, `datatypes/UpdateInfo` | ⬜ |
| Android TV (leanback) UI: rows, cards, player, logins | `tv/*` (MainFragment, VideosFragment, VideoPlayerTV, CardPresenter…) | ⬜ (leanback dep declared) |
| AdMob banner/interstitial | `datatypes/AdSelection`, play-services-ads | ⬜ (ads dep declared) |
| Firebase Analytics + Crashlytics | manifest providers | ⬜ (see app/google-services.json.template) |
| Internet connectivity guard | `methods/CheckInternet` | ⬜ |

## 8. Crack artifacts removed in the revival
The supplied APK was a modified "Premium" build, not a clean release. The rewrite
deliberately drops the tamper scaffolding and restores correct behaviour:

- `LoginStatus.getPro()` / `setPro()` were patched to ignore the backend and return
  a hard-coded far-future timestamp (`2543509804498`), forcing permanent PRO. The
  reconstructed `LoginStatus` honours the real server value.
- `bin.mt.signature.KillerApplication` (ApkSignatureKillerEx) + `org.lsposed.hiddenapibypass`
  spoofed the signing certificate to defeat the `hash` check. Dropped; the revived
  `StreamerApp` extends plain `Application` and the real signing cert is used.

Site tags, scraping recipes and paysite lists are server-driven (`v7/sites`,
`v7/getInfo`) rather than hard-coded, so the catalogue updates without an app release.
