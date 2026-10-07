package com.streamdev.aiostreamer.baseline.model

import com.google.gson.annotations.SerializedName

/**
 * Data-transfer objects for the backend API, reconstructed from the v6.4.5
 * `datatypes` package. Field names and `@SerializedName` keys are preserved so
 * the wire format is byte-compatible with the existing backend.
 */

// ---------------------------------------------------------------------------
// Request bodies
// ---------------------------------------------------------------------------

/** Login request body. The password is sent as its SHA3-256 hex digest, never in clear. */
data class UserData(
    @SerializedName("username") var username: String? = null,
    @SerializedName("password") var password: String? = null,
    @SerializedName("android_id") var androidId: String? = null,
)

/** Opaque, server-defined payload wrapper used by `v7/getData`. */
data class PayloadData(
    @SerializedName("payload") var payload: String? = null,
)

/** Filter applied to list/search queries. */
data class StandardFilter(
    @SerializedName("viewer") var viewer: String = "new",
    @SerializedName("page") var page: Int = 1,
    @SerializedName("category") var category: Boolean = false,
    @SerializedName("gay") var gay: Boolean = false,
)

/** Body for `v7/getInfo` and `v7/getLink`: the site tag plus the active filter. */
data class GetSiteInfo(
    @SerializedName("site") var site: String,
    @SerializedName("filter") var filter: StandardFilter,
)

/** Body for `v7/videoheaders`: describes a resolved stream the player is about to open. */
data class VideoObject(
    @SerializedName("streamLink") var streamLink: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("sourceLink") var sourceLink: String? = null,
    @SerializedName("hosterLink") var hosterLink: String? = null,
    @SerializedName("test") var test: Boolean = false,
    @SerializedName("image") var image: String? = null,
    @SerializedName("duration") var duration: String? = null,
    @SerializedName("webm") var webm: String? = null,
    @SerializedName("download") var download: Boolean = false,
    @SerializedName("hosterSite") var hosterSite: String? = null,
    @SerializedName("pro") var pro: Boolean = false,
    @SerializedName("site") var site: String? = null,
)

/** Crash/scrape error report body for `v7/error`. */
data class GenericError(
    @SerializedName("message") var message: String? = null,
    @SerializedName("reason") var reason: String? = null,
    @SerializedName("site") var site: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("version") var version: Int = 0,
    @SerializedName("errors") var errors: MutableList<String> = mutableListOf(),
    @SerializedName("androidVersion") var androidVersion: String = "",
)

// ---------------------------------------------------------------------------
// Shared video model (request body for favorites/history, and list responses)
// ---------------------------------------------------------------------------

data class VideoInformation(
    @SerializedName("fav_id") var favId: Int = 0,
    @SerializedName("link") var link: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("img") var img: String? = null,
    @SerializedName("duration") var duration: String? = null,
    @SerializedName("webm") var webm: String? = null,
    @SerializedName("site") var site: String? = null,
)

// ---------------------------------------------------------------------------
// Response bodies
// ---------------------------------------------------------------------------

/** Generic `{ result, statusCode }` envelope returned by most mutating endpoints. */
data class SimpleResult(
    @SerializedName("result") var result: String? = null,
    @SerializedName("statusCode") var statusCode: Int = 0,
)

data class FavoriteResult(
    @SerializedName("fav_id") var favId: Int = 0,
    @SerializedName("statusCode") var statusCode: Int = 0,
    @SerializedName("statusMessage") var statusMessage: String? = null,
)

data class Categories(
    @SerializedName("item") var item: List<String>? = null,
)

data class StarterMethod(
    @SerializedName("starter") var starter: String? = null,
)

/**
 * Result of `v7/login`.
 *
 * NOTE: in the supplied v6.4.5 "Premium" APK the `getPro()` accessor had been
 * patched to return a hard-coded far-future timestamp (a crack artifact). The
 * revival restores the correct behaviour: [pro] is whatever the backend returns,
 * i.e. the unix time the user's PRO membership expires (0 / past = not PRO).
 */
data class LoginStatus(
    @SerializedName("pro") var pro: Long = 0L,
    @SerializedName("status") var status: Int = 0,
    @SerializedName("token") var token: String? = null,
    @SerializedName("unixtime") var unixtime: Long = 0L,
) {
    /** True when the backend reported a membership expiry later than its server clock. */
    fun isPro(): Boolean = pro > unixtime

    /** True for a 2xx login status. */
    fun isSuccess(): Boolean = status in 200..299
}

/** Per-video streaming headers returned by `v7/videoheaders`. */
data class VideoHeaders(
    @SerializedName("headers") var headers: Map<String, String> = emptyMap(),
    @SerializedName("m3u8") var m3u8: Boolean = false,
    @SerializedName("cookies") var cookies: Boolean = false,
    @SerializedName("useragent") var userAgent: String? = null,
    @SerializedName("site") var site: String = "",
)

/** Resolved playable link for `v7/getLink`. */
data class GetLink(
    @SerializedName("link") var link: String? = null,
)

/** One entry in the site catalogue returned by `v7/sites`. */
data class SiteInfo(
    @SerializedName("name") var name: String? = null,
    @SerializedName("sitetag") var sitetag: String? = null,
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

/** Scraping recipe for a single site, returned by `v7/getInfo`. */
data class SiteInformation(
    @SerializedName("relatedVideos") var relatedVideos: Boolean = false,
    @SerializedName("protocol") var protocol: String? = null,
    @SerializedName("site") var site: String? = null,
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
)

/** A user-defined cloud favorites playlist (`v7/playlists`). */
data class CloudPlaylist(
    @SerializedName("playlist_id") var playlistId: Int = 0,
    @SerializedName("playlist_name") var playlistName: String? = null,
    @SerializedName("user_id") var userId: Int = 0,
)

/** A site name as returned by `v7/favorites/sites` and `v7/history/sites`. */
data class CloudSiteList(
    @SerializedName("site") var site: String? = null,
)
