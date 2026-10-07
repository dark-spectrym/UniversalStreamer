package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.model.Categories
import com.streamdev.aiostreamer.baseline.model.CloudPlaylist
import com.streamdev.aiostreamer.baseline.model.CloudSiteList
import com.streamdev.aiostreamer.baseline.model.FavoriteResult
import com.streamdev.aiostreamer.baseline.model.GenericError
import com.streamdev.aiostreamer.baseline.model.GetLink
import com.streamdev.aiostreamer.baseline.model.GetSiteInfo
import com.streamdev.aiostreamer.baseline.model.LoginStatus
import com.streamdev.aiostreamer.baseline.model.PayloadData
import com.streamdev.aiostreamer.baseline.model.SimpleResult
import com.streamdev.aiostreamer.baseline.model.SiteInfo
import com.streamdev.aiostreamer.baseline.model.SiteInformation
import com.streamdev.aiostreamer.baseline.model.StarterMethod
import com.streamdev.aiostreamer.baseline.model.VideoHeaders
import com.streamdev.aiostreamer.baseline.model.VideoInformation
import com.streamdev.aiostreamer.baseline.model.VideoObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Typed contract for the backend REST API (`/api/v7`).
 *
 * Reconstructed one-to-one from the v6.4.5 `ApiService`, with two improvements:
 *  - RxJava3 `Observable<T>` return types are replaced with coroutine `suspend` functions.
 *  - The `hash` and `Authorization` headers that were repeated on every method are
 *    gone; [com.streamdev.aiostreamer.baseline.net.AuthInterceptor] attaches them.
 *    Endpoints that must NOT carry a bearer token are annotated `@Headers("X-No-Auth: 1")`.
 *
 * This interface is the single source of truth for every backend connection the
 * app makes — the "API baseline" for outside connections.
 */
interface ApiService {

    // --- Session / account ---

    @POST("v7/login")
    suspend fun login(@Body user: com.streamdev.aiostreamer.baseline.model.UserData): LoginStatus

    @FormUrlEncoded
    @POST("v7/device")
    suspend fun addDevice(@Field("android_id") androidId: String): SimpleResult

    @Headers("X-No-Auth: 1")
    @FormUrlEncoded
    @POST("v7/unixTime")
    suspend fun getUnixTime(
        @Field("deviceUnixTime") deviceUnixTime: Long,
        @Field("version") version: Int,
    ): SimpleResult

    @Headers("X-No-Auth: 1")
    @GET("v7/checkInfo")
    suspend fun checkInfo(): Map<String, Any?>

    // --- Tokens / coins / PRO economy ---

    @GET("v7/tokens")
    suspend fun getTokens(): SimpleResult

    @POST("v7/tokens")
    suspend fun addDailyToken(): SimpleResult

    @FormUrlEncoded
    @POST("v7/coins")
    suspend fun getCoins(@Field("hwid") hwid: String, @Field("id") id: String): SimpleResult

    @POST("v7/exchange")
    suspend fun exchangeCoins(@Query("hwid") hwid: String): SimpleResult

    // --- Site catalogue & scraping recipes ---

    @Headers("X-No-Auth: 1")
    @GET("v7/sites")
    suspend fun getSites(): Map<String, List<SiteInfo>>

    @Headers("X-No-Auth: 1")
    @GET("v7/categories")
    suspend fun getCategories(@Query("site") site: String): Categories

    @Headers("X-No-Auth: 1")
    @GET("v7/starter")
    suspend fun getStarterMethod(@Query("site") site: String): StarterMethod

    @Headers("X-No-Auth: 1")
    @POST("v7/getInfo")
    suspend fun getSiteInformation(@Body info: GetSiteInfo): SiteInformation

    // --- Content & stream resolution ---

    @POST("v7/getData")
    suspend fun getData(
        @Body payload: PayloadData,
        @Query("sitetag") siteTag: String,
        @Query("isTV") isTv: Boolean,
        @Query("gay") gay: Boolean,
    ): List<VideoInformation>

    @POST("v7/getLink")
    suspend fun getLink(@Body info: GetSiteInfo): GetLink

    @Headers("X-No-Auth: 1")
    @POST("v7/videoheaders")
    suspend fun getVideoHeaders(@Body video: VideoObject): VideoHeaders

    // --- Favorites ---

    @GET("v7/favorites")
    suspend fun getFavorites(
        @Query("order") order: String,
        @Query("site") site: String,
        @Query("search") search: String,
        @Query("page") page: Int,
        @Query("playlist") playlist: Int,
    ): List<VideoInformation>

    @POST("v7/favorites")
    suspend fun addFavorite(@Body video: VideoInformation): FavoriteResult

    @DELETE("v7/favorites")
    suspend fun deleteFavorites(@Query("fav_ids[]") favIds: List<Int>): SimpleResult

    @GET("v7/favorites/check")
    suspend fun checkFavorite(@Query("link") link: String): FavoriteResult

    @GET("v7/favorites/check/id")
    suspend fun checkFavoriteId(@Query("link") link: String): FavoriteResult

    @GET("v7/favorites/sites")
    suspend fun getFavoritesSites(): List<CloudSiteList>

    // --- Favorites playlists ---

    @GET("v7/playlists")
    suspend fun getFavoritesPlaylists(): List<CloudPlaylist>

    @POST("v7/playlists")
    suspend fun addFavoritePlaylist(@Query("playlist_name") playlistName: String): SimpleResult

    @POST("v7/playlists/favorites")
    suspend fun addPlaylistWithVideo(
        @Body video: VideoInformation,
        @Query("playlist_name") playlistName: String,
    ): SimpleResult

    @PUT("v7/playlists/{playlistId}")
    suspend fun addVideoToFavoritePlaylist(
        @Body video: VideoInformation,
        @Path("playlistId") playlistId: Int,
    ): SimpleResult

    @DELETE("v7/playlists/{playlistId}")
    suspend fun deleteFavoritesPlaylist(@Path("playlistId") playlistId: Int): SimpleResult

    @DELETE("v7/playlists/{playlistId}/favorites")
    suspend fun deleteVideoFromFavoritesPlaylist(
        @Path("playlistId") playlistId: Int,
        @Query("fav_ids[]") favIds: List<Int>,
    ): SimpleResult

    // --- History ---

    @GET("v7/history")
    suspend fun getHistory(
        @Query("order") order: String,
        @Query("site") site: String,
        @Query("search") search: String,
        @Query("page") page: Int,
    ): List<VideoInformation>

    @POST("v7/history")
    suspend fun addHistory(@Body video: VideoInformation): SimpleResult

    @DELETE("v7/history")
    suspend fun deleteHistory(): SimpleResult

    @GET("v7/history/sites")
    suspend fun getHistorySites(): List<CloudSiteList>

    // --- Diagnostics ---

    @Headers("X-No-Auth: 1")
    @POST("v7/error")
    suspend fun sendError(@Body error: GenericError): SimpleResult
}
