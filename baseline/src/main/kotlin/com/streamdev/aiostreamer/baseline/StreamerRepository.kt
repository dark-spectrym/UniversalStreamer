package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.model.Categories
import com.streamdev.aiostreamer.baseline.model.CloudPlaylist
import com.streamdev.aiostreamer.baseline.model.FavoriteResult
import com.streamdev.aiostreamer.baseline.model.GetLink
import com.streamdev.aiostreamer.baseline.model.GetSiteInfo
import com.streamdev.aiostreamer.baseline.model.LoginStatus
import com.streamdev.aiostreamer.baseline.model.SimpleResult
import com.streamdev.aiostreamer.baseline.model.SiteInfo
import com.streamdev.aiostreamer.baseline.model.SiteInformation
import com.streamdev.aiostreamer.baseline.model.StandardFilter
import com.streamdev.aiostreamer.baseline.model.UserData
import com.streamdev.aiostreamer.baseline.model.VideoHeaders
import com.streamdev.aiostreamer.baseline.model.VideoInformation
import com.streamdev.aiostreamer.baseline.model.VideoObject
import com.streamdev.aiostreamer.baseline.net.CredentialStore

/**
 * High-level, coroutine-based gateway over [ApiService].
 *
 * Every public method returns a [Result] so callers handle success and failure
 * without try/catch blocks or the RxJava `Observer` boilerplate the original UI
 * repeated at each call site. Cross-cutting side effects (persisting the bearer
 * token after login) live here rather than in the UI.
 */
class StreamerRepository(
    private val api: ApiService,
    private val credentials: CredentialStore,
) {

    // --- Session ---

    /** Logs in and, on success, persists the returned bearer token. */
    suspend fun login(username: String, passwordSha3Hex: String, androidId: String): Result<LoginStatus> =
        runCatching {
            val status = api.login(UserData(username, passwordSha3Hex, androidId))
            status.token?.takeIf { it.isNotEmpty() }?.let(credentials::updateAccessToken)
            status
        }

    fun logout() = credentials.clear()

    // --- Catalogue ---

    suspend fun sites(): Result<Map<String, List<SiteInfo>>> = runCatching { api.getSites() }

    suspend fun siteInformation(site: String, filter: StandardFilter): Result<SiteInformation> =
        runCatching { api.getSiteInformation(GetSiteInfo(site, filter)) }

    suspend fun categories(site: String): Result<Categories> = runCatching { api.getCategories(site) }

    // --- Content ---

    suspend fun data(
        payload: com.streamdev.aiostreamer.baseline.model.PayloadData,
        siteTag: String,
        isTv: Boolean,
        gay: Boolean,
    ): Result<List<VideoInformation>> = runCatching { api.getData(payload, siteTag, isTv, gay) }

    suspend fun resolveLink(site: String, filter: StandardFilter): Result<GetLink> =
        runCatching { api.getLink(GetSiteInfo(site, filter)) }

    suspend fun videoHeaders(video: VideoObject): Result<VideoHeaders> =
        runCatching { api.getVideoHeaders(video) }

    // --- Favorites ---

    suspend fun favorites(order: String, site: String, search: String, page: Int, playlist: Int):
        Result<List<VideoInformation>> = runCatching { api.getFavorites(order, site, search, page, playlist) }

    suspend fun addFavorite(video: VideoInformation): Result<FavoriteResult> =
        runCatching { api.addFavorite(video) }

    suspend fun deleteFavorites(favIds: List<Int>): Result<SimpleResult> =
        runCatching { api.deleteFavorites(favIds) }

    suspend fun playlists(): Result<List<CloudPlaylist>> = runCatching { api.getFavoritesPlaylists() }

    // --- History ---

    suspend fun history(order: String, site: String, search: String, page: Int):
        Result<List<VideoInformation>> = runCatching { api.getHistory(order, site, search, page) }

    suspend fun addHistory(video: VideoInformation): Result<SimpleResult> =
        runCatching { api.addHistory(video) }

    suspend fun clearHistory(): Result<SimpleResult> = runCatching { api.deleteHistory() }

    // --- Economy ---

    suspend fun tokens(): Result<SimpleResult> = runCatching { api.getTokens() }

    suspend fun claimDailyToken(): Result<SimpleResult> = runCatching { api.addDailyToken() }
}
