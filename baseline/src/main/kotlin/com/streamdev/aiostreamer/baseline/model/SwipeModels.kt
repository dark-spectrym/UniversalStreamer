package com.streamdev.aiostreamer.baseline.model

import com.google.gson.annotations.SerializedName

/**
 * Models for the two auxiliary backends used by the NSFW swipe feed:
 * `nsfwswipe.com/api/` (categories + items) and `api.redgifs.com/v2/` (gif URLs).
 * Reconstructed from the v6.7.1 `datatypes.swipe` package.
 */

/** A swipe-feed category (`nsfwswipe.com/api/getCategories`). */
data class SwipeCategory(
    @SerializedName("subreddit") var subreddit: String? = null,
    @SerializedName("count") var count: Int = 0,
)

/** A single swipe-feed item (`nsfwswipe.com/api/getVideos/{id}/{orientation}`). */
data class SwipeItem(
    @SerializedName("swipeId") var swipeId: Long = 0L,
    @SerializedName("title") var title: String? = null,
    @SerializedName("subreddit") var subreddit: String? = null,
    @SerializedName("url") var url: String? = null,
    @SerializedName("imgurl") var imgUrl: String? = null,
    @SerializedName("mp4_url") var mp4Url: String? = null,
    @SerializedName("ifr") var iframe: String? = null,
    @SerializedName("streamLink") var streamLink: String = "",
    @SerializedName("likes") var likes: Int = 0,
    @SerializedName("views") var views: Int = 0,
)

/** Feed orientation filter. */
enum class SwipeOrientation(val value: String) {
    BOTH("both"),
    HORIZONTAL("horizontal"),
    VERTICAL("vertical"),
}

/** RedGifs temporary-auth token (`api.redgifs.com/v2/auth/temporary`). */
data class RedgifsAuth(
    @SerializedName("token") var token: String? = null,
    @SerializedName("addr") var addr: String? = null,
    @SerializedName("agent") var agent: String? = null,
    @SerializedName("rtfm") var rtfm: String? = null,
    @SerializedName("session") var session: String? = null,
)

/** RedGifs gif response with HD/SD URLs. */
data class GifResponse(
    @SerializedName("gif") var gif: Gif? = null,
) {
    data class Gif(
        @SerializedName("urls") var urls: Urls? = null,
    )

    data class Urls(
        @SerializedName("hd") var hd: String? = null,
        @SerializedName("sd") var sd: String? = null,
    )
}
