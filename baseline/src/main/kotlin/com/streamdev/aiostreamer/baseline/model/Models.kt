package com.streamdev.aiostreamer.baseline.model

import com.google.gson.annotations.SerializedName

/**
 * Data-transfer objects for the v9 backend API, reconstructed from the v6.7.1
 * `datatypes` package. `@SerializedName` keys match the wire format exactly.
 */

// ---------------------------------------------------------------------------
// Session / account
// ---------------------------------------------------------------------------

/** Login request body. Password is sent as its SHA3-256 hex digest. */
data class UserData(
    @SerializedName("username") var username: String? = null,
    @SerializedName("password") var password: String? = null,
    @SerializedName("android_id") var androidId: String? = null,
)

/**
 * Result of `v9/login`, and the login snapshot embedded in the request hash.
 * `pro` is the unix expiry of PRO membership (0/past = not PRO) straight from the
 * server (the v6.4.5 forced-PRO crack is not reproduced).
 */
data class LoginStatus(
    @SerializedName("pro") var pro: Long = 0L,
    @SerializedName("status") var status: Int = 0,
    @SerializedName("token") var token: String? = null,
    @SerializedName("unixtime") var unixtime: Long = 0L,
    @SerializedName("user_id") var userId: Int = 0,
) {
    fun isPro(): Boolean = pro > unixtime
    fun isSuccess(): Boolean = status in 200..299
}

// ---------------------------------------------------------------------------
// Filters
// ---------------------------------------------------------------------------

/** Base list/search filter (v9 dropped the v7 `gay` flag; it is a query param now). */
open class StandardFilter(
    @SerializedName("viewer") var viewer: String = "new",
    @SerializedName("page") var page: Int = 1,
    @SerializedName("category") var category: Boolean = false,
)

/** Extended filter for the `v9/porndb` metadata search. */
class PornDBFilter(
    viewer: String = "new",
    page: Int = 1,
    category: Boolean = false,
    @SerializedName("duration") var duration: String = "all",
    @SerializedName("order") var order: String = "new",
    @SerializedName("quality") var quality: String? = null,
    @SerializedName("fulltext") var fulltext: String? = null,
    @SerializedName("pornstars") var pornstars: List<String> = emptyList(),
    @SerializedName("studios") var studios: List<String> = emptyList(),
    @SerializedName("tags") var tags: List<String> = emptyList(),
    @SerializedName("sites") var sites: List<String> = emptyList(),
) : StandardFilter(viewer, page, category)

// ---------------------------------------------------------------------------
// Site requests / data
// ---------------------------------------------------------------------------

/** Body for `v9/sites/{sitetag}/info` and `.../link`. */
data class SiteInfoRequest(
    @SerializedName("site") var site: String,
    @SerializedName("filter") var filter: StandardFilter,
    @SerializedName("globalSearch") var globalSearch: Boolean = false,
    @SerializedName("porntabs") var pornTabs: Boolean = false,
)

/** Body for `v9/sites/{sitetag}/data` and `.../related`. */
data class SiteData(
    @SerializedName("link") var link: String? = null,
    @SerializedName("payload") var payload: String? = null,
)

/** Body for `v9/sites/{sitetag}/stream`. */
data class StreamData(
    @SerializedName("payload") var payload: String? = null,
    @SerializedName("videoObject") var videoObject: VideoObject? = null,
)

/** Body for `v9/sites/{sitetag}/tags`. */
data class TagData(
    @SerializedName("link") var link: String? = null,
    @SerializedName("payload") var payload: String? = null,
)

/** One entry in the site catalogue returned by `v9/sites`. */
data class SiteInfo(
    @SerializedName("name") var name: String? = null,
    @SerializedName("sitetag") var sitetag: String? = null,
    @SerializedName("site_id") var siteId: Int = 0,
    @SerializedName("image") var image: String? = null,
    @SerializedName("packagename") var packagename: String? = null,
    @SerializedName("online") var online: Boolean = false,
    @SerializedName("newSite") var newSite: Boolean = false,
    @SerializedName("preview") var preview: Boolean = false,
    @SerializedName("search") var search: Boolean = false,
    @SerializedName("test") var test: Boolean = false,
    @SerializedName("tv") var tv: Boolean = false,
    @SerializedName("gay") var gay: Boolean = false,
    @Transient var favorite: Boolean = false,
)

/** Scraping recipe for a site, returned by `v9/sites/{sitetag}/info`. */
data class SiteInformation(
    @SerializedName("relatedVideos") var relatedVideos: Boolean = false,
    @SerializedName("protocol") var protocol: String? = null,
    @SerializedName("site") var site: String? = null,
    @SerializedName("sitetag") var sitetag: String? = null,
    @SerializedName("base") var base: String? = null,
    @SerializedName("newUrl") var newUrl: String? = null,
    @SerializedName("mvUrl") var mvUrl: String? = null,
    @SerializedName("hotUrl") var hotUrl: String? = null,
    @SerializedName("searchUrl") var searchUrl: String? = null,
    @SerializedName("categoriesUrl") var categoriesUrl: String? = null,
    @SerializedName("searchReplacer") var searchReplacer: String? = null,
    @SerializedName("categoryReplacer") var categoryReplacer: String? = null,
    @SerializedName("listDiv") var listDiv: String? = null,
    @SerializedName("listSelector") var listSelector: Int = 0,
    @SerializedName("videoDiv") var videoDiv: String? = null,
    @SerializedName("listDivRelated") var listDivRelated: String? = null,
    @SerializedName("listSelectorRelated") var listSelectorRelated: Int = 0,
    @SerializedName("videoDivRelated") var videoDivRelated: String? = null,
    @SerializedName("cookies") var cookies: Boolean = false,
    @SerializedName("filter") var filter: String? = null,
)

/** Category list for a site (`v9/categories`). */
data class SiteCategories(
    @SerializedName("item") var item: List<String>? = null,
)

/** Resolved playable link for `v9/sites/{sitetag}/link`. */
data class LinkResponse(
    @SerializedName("link") var link: String? = null,
)

// ---------------------------------------------------------------------------
// Video models
// ---------------------------------------------------------------------------

data class VideoInformation(
    @SerializedName("video_id") var videoId: Int = 0,
    @SerializedName("fav_id") var favId: Int = 0,
    @SerializedName("site_id") var siteId: Int = 0,
    @SerializedName("link") var link: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("img") var img: String? = null,
    @SerializedName("duration") var duration: String? = null,
    @SerializedName("webm") var webm: String? = null,
    @SerializedName("site") var site: String? = null,
    @SerializedName("sitetag") var sitetag: String? = null,
    @SerializedName("quality") var quality: Int = 0,
    @SerializedName("pornstars") var pornstars: List<String>? = null,
)

/** A single resolved stream variant. */
data class VideoLink(
    @SerializedName("quality") var quality: String? = null,
    @SerializedName("stream") var streamLink: String? = null,
    @SerializedName("type") var type: String? = null,
)

/** Body for stream/header resolution and the `video/{id}/info` report. */
data class VideoObject(
    @SerializedName("streamLink") var streamLink: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("sourceLink") var sourceLink: String? = null,
    @SerializedName("hosterLink") var hosterLink: String? = null,
    @SerializedName("embedLink") var embedLink: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("duration") var duration: String? = null,
    @SerializedName("seconds") var seconds: Long = 0L,
    @SerializedName("webm") var webm: String? = null,
    @SerializedName("download") var download: Boolean = false,
    @SerializedName("hosterSite") var hosterSite: String? = null,
    @SerializedName("sitename") var sitename: String? = null,
    @SerializedName("site") var site: String? = null,
    @SerializedName("quality") var quality: Int = 0,
    @SerializedName("video_id") var videoId: Int = 0,
    @SerializedName("streams") var streams: List<VideoLink>? = null,
    @SerializedName("testMode") var testMode: TestMode? = null,
)

/** Per-stream playback headers (`v9/videoheaders`). */
data class VideoHeaders(
    @SerializedName("headers") var headers: Map<String, String> = emptyMap(),
    @SerializedName("m3u8") var m3u8: Boolean = false,
    @SerializedName("cookies") var cookies: Boolean = false,
    @SerializedName("fallback") var fallback: Boolean = false,
    @SerializedName("live") var live: Boolean = false,
    @SerializedName("useragent") var userAgent: String? = null,
    @SerializedName("site") var site: String = "",
)

// ---------------------------------------------------------------------------
// Stream resolution extras
// ---------------------------------------------------------------------------

data class StarterMethod(
    @SerializedName("starter") var starter: String? = null,
)

/** Result of `v9/sites/{sitetag}/tags`: metadata scraped for a video. */
data class TagResult(
    @SerializedName("video_id") var videoId: Int = 0,
    @SerializedName("title") var title: String? = null,
    @SerializedName("tags") var tags: List<String>? = null,
    @SerializedName("pornstars") var pornstars: List<String>? = null,
    @SerializedName("studios") var studios: List<String>? = null,
)

/** Result of `v9/sites/{sitetag}/extra`: extra buttons / errors / m3u8 candidates. */
data class ExtraInfoParser(
    @SerializedName("buttons") var buttons: List<String>? = null,
    @SerializedName("errors") var errors: List<String>? = null,
    @SerializedName("m3u8") var m3u8: List<String>? = null,
)

// ---------------------------------------------------------------------------
// Favorites / history / playlists
// ---------------------------------------------------------------------------

data class FavoriteResult(
    @SerializedName("fav_id") var favId: Int = 0,
    @SerializedName("statusCode") var statusCode: Int = 0,
    @SerializedName("statusMessage") var statusMessage: String? = null,
)

data class CloudPlaylist(
    @SerializedName("playlist_id") var playlistId: Int = 0,
    @SerializedName("playlist_name") var playlistName: String? = null,
    @SerializedName("user_id") var userId: Int = 0,
    @SerializedName("favorites_count") var favoritesCount: Int = 0,
)

data class CloudSiteList(
    @SerializedName("site") var site: String? = null,
)

// ---------------------------------------------------------------------------
// Economy / cast / diagnostics / update
// ---------------------------------------------------------------------------

/** Generic `{ result, statusCode }` envelope. */
data class SimpleResult(
    @SerializedName("result") var result: String? = null,
    @SerializedName("statusCode") var statusCode: Int = 0,
)

/** Response of `v9/coins/check`: coin balance vs. the amount needed to unlock. */
data class ConversionResponse(
    @SerializedName("pro") var pro: Int = 0,
    @SerializedName("coins") var coins: Int = 0,
    @SerializedName("needed") var needed: Int = 0,
)

/** Body for `v9/tv/send`: hand a video off to a paired TV session. */
data class VideoToTv(
    @SerializedName("video_id") var videoId: Int = 0,
    @SerializedName("action") var action: VideoToTvAction = VideoToTvAction.PLAY_DIRECTLY,
    @SerializedName("cookies") var cookies: String? = null,
) {
    enum class VideoToTvAction { PLAY_DIRECTLY, ADD_TO_PLAYERPLAYLIST }
}

/** Client-side error report body for `v9/error`. */
data class ClientError(
    @SerializedName("message") var message: String? = null,
    @SerializedName("reason") var reason: String? = null,
    @SerializedName("site") var site: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("version") var version: Int = 0,
    @SerializedName("androidVersion") var androidVersion: String = "",
    @SerializedName("errors") var errors: MutableList<String> = mutableListOf(),
    @SerializedName("loginStatus") var loginStatus: LoginStatus? = null,
)

/** Error envelope returned by `v9/error`. */
data class ErrorResponse(
    @SerializedName("status") var status: Int = 0,
    @SerializedName("message") var message: String? = null,
    @SerializedName("error") var error: String? = null,
)

/** In-app update info (`update`). */
data class UpdateResponse(
    @SerializedName("latestVersion") var latestVersion: String? = null,
    @SerializedName("latestVersionCode") var latestVersionCode: Int = 0,
    @SerializedName("releaseNotes") var releaseNotes: String? = null,
    @SerializedName("url") var url: String? = null,
)

/** Server-driven test/download mode for a resolved video. */
enum class TestMode {
    STREAM,
    STREAM_AND_DOWNLOAD,
    STREAM_AND_DOWNLOAD_AND_M3U8,
}
