package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.model.CloudPlaylist
import com.streamdev.aiostreamer.baseline.model.ConversionResponse
import com.streamdev.aiostreamer.baseline.model.FavoriteResult
import com.streamdev.aiostreamer.baseline.model.LinkResponse
import com.streamdev.aiostreamer.baseline.model.LoginStatus
import com.streamdev.aiostreamer.baseline.model.PornDBFilter
import com.streamdev.aiostreamer.baseline.model.SimpleResult
import com.streamdev.aiostreamer.baseline.model.SiteData
import com.streamdev.aiostreamer.baseline.model.SiteInfo
import com.streamdev.aiostreamer.baseline.model.SiteInfoRequest
import com.streamdev.aiostreamer.baseline.model.SiteInformation
import com.streamdev.aiostreamer.baseline.model.StandardFilter
import com.streamdev.aiostreamer.baseline.model.StreamData
import com.streamdev.aiostreamer.baseline.model.UserData
import com.streamdev.aiostreamer.baseline.model.VideoHeaders
import com.streamdev.aiostreamer.baseline.model.VideoInformation
import com.streamdev.aiostreamer.baseline.model.VideoLink
import com.streamdev.aiostreamer.baseline.model.VideoObject
import com.streamdev.aiostreamer.baseline.net.CredentialStore

/**
 * High-level, coroutine-based gateway over the v9 [ApiService].
 *
 * Returns [Result] from every call so callers skip the RxJava `Observer`
 * boilerplate the original UI repeated at each site. Login's token-persistence
 * side effect lives here.
 */
class StreamerRepository(
    private val api: ApiService,
    private val credentials: CredentialStore,
) {

    // --- Session ---

    suspend fun login(username: String, passwordSha3Hex: String, androidId: String): Result<LoginStatus> =
        runCatching {
            val status = api.login(UserData(username, passwordSha3Hex, androidId))
            status.token?.takeIf { it.isNotEmpty() }?.let(credentials::updateAccessToken)
            status
        }

    fun logout() = credentials.clear()

    // --- Catalogue ---

    suspend fun sites(): Result<Map<String, List<SiteInfo>>> = runCatching { api.getSites() }

    suspend fun siteInfo(siteTag: String, request: SiteInfoRequest): Result<SiteInformation> =
        runCatching { api.getSiteInfo(request, siteTag) }

    // --- Content ---

    suspend fun data(siteTag: String, body: SiteData, isTv: Boolean): Result<List<VideoInformation>> =
        runCatching { api.getData(body, siteTag, isTv) }

    suspend fun related(siteTag: String, body: SiteData, isTv: Boolean): Result<List<VideoInformation>> =
        runCatching { api.getRelated(body, siteTag, isTv) }

    suspend fun link(siteTag: String, request: SiteInfoRequest): Result<LinkResponse> =
        runCatching { api.getLink(request, siteTag) }

    suspend fun stream(siteTag: String, body: StreamData, isTv: Boolean): Result<List<VideoLink>> =
        runCatching { api.getStream(body, siteTag, isTv) }

    suspend fun videoHeaders(video: VideoObject): Result<VideoHeaders> =
        runCatching { api.getVideoHeaders(video) }

    suspend fun pornDb(filter: PornDBFilter): Result<List<VideoInformation>> =
        runCatching { api.pornDb(filter) }

    // --- Favorites ---

    suspend fun favorites(order: String, site: String, search: String, page: Int, playlist: Int, seed: Int):
        Result<List<VideoInformation>> = runCatching { api.getFavorites(order, site, search, page, playlist, seed) }

    suspend fun addFavorite(video: VideoInformation): Result<FavoriteResult> =
        runCatching { api.addFavorite(video) }

    suspend fun deleteFavorites(favIds: List<Int>): Result<SimpleResult> =
        runCatching { api.deleteFavorites(favIds) }

    suspend fun playlists(): Result<List<CloudPlaylist>> = runCatching { api.getPlaylists() }

    // --- History ---

    suspend fun history(order: String, site: String, search: String, page: Int):
        Result<List<VideoInformation>> = runCatching { api.getHistory(order, site, search, page) }

    suspend fun addHistory(video: VideoInformation): Result<SimpleResult> =
        runCatching { api.addHistory(video) }

    suspend fun clearHistory(): Result<SimpleResult> = runCatching { api.deleteHistory() }

    // --- Coins economy ---

    suspend fun coinsCheck(m3u8Id: String): Result<ConversionResponse> =
        runCatching { api.coinsCheck(m3u8Id) }

    suspend fun coinsExchange(m3u8Id: String): Result<SimpleResult> =
        runCatching { api.coinsExchange(m3u8Id) }

    /** Convenience for the common site list + default filter. */
    fun defaultFilter(page: Int = 1): StandardFilter = StandardFilter(page = page)
}
