package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.model.ClientError
import com.streamdev.aiostreamer.baseline.model.CloudPlaylist
import com.streamdev.aiostreamer.baseline.model.CloudSiteList
import com.streamdev.aiostreamer.baseline.model.ConversionResponse
import com.streamdev.aiostreamer.baseline.model.ErrorResponse
import com.streamdev.aiostreamer.baseline.model.ExtraInfoParser
import com.streamdev.aiostreamer.baseline.model.FavoriteResult
import com.streamdev.aiostreamer.baseline.model.LinkResponse
import com.streamdev.aiostreamer.baseline.model.LoginStatus
import com.streamdev.aiostreamer.baseline.model.PornDBFilter
import com.streamdev.aiostreamer.baseline.model.SimpleResult
import com.streamdev.aiostreamer.baseline.model.SiteCategories
import com.streamdev.aiostreamer.baseline.model.SiteData
import com.streamdev.aiostreamer.baseline.model.SiteInfo
import com.streamdev.aiostreamer.baseline.model.SiteInfoRequest
import com.streamdev.aiostreamer.baseline.model.SiteInformation
import com.streamdev.aiostreamer.baseline.model.StarterMethod
import com.streamdev.aiostreamer.baseline.model.StreamData
import com.streamdev.aiostreamer.baseline.model.TagData
import com.streamdev.aiostreamer.baseline.model.TagResult
import com.streamdev.aiostreamer.baseline.model.UpdateResponse
import com.streamdev.aiostreamer.baseline.model.UserData
import com.streamdev.aiostreamer.baseline.model.VideoHeaders
import com.streamdev.aiostreamer.baseline.model.VideoInformation
import com.streamdev.aiostreamer.baseline.model.VideoObject
import com.streamdev.aiostreamer.baseline.model.VideoToTv
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Typed contract for the v9 backend REST API (`porn-app.com/api/v9`).
 *
 * Reconstructed from the clean v6.7.1 client. Compared with v7 this is a RESTful
 * redesign: per-site resources live under `v9/sites/{sitetag}/…`, tokens became
 * `coins`, and `porndb` / `tv/send` were added.
 *
 * Improvements over the shipped interface:
 *  - RxJava `Observable<T>` is replaced with coroutine `suspend` functions.
 *  - The `hash` + `Authorization` headers repeated on every method are injected by
 *    [com.streamdev.aiostreamer.baseline.net.AuthInterceptor]; endpoints that must
 *    not carry a bearer token are tagged `@Headers("X-No-Auth: 1")`.
 */
interface ApiService {

    // --- Session / account ---

    @POST("v9/login")
    suspend fun login(@Body user: UserData): LoginStatus

    @FormUrlEncoded
    @POST("v9/device")
    suspend fun registerDevice(@Field("android_id") androidId: String): SimpleResult

    @Headers("X-No-Auth: 1")
    @FormUrlEncoded
    @POST("v9/unixTime")
    suspend fun getUnixTime(
        @Field("deviceUnixTime") deviceUnixTime: Long,
        @Field("version") version: Int,
    ): SimpleResult

    @Headers("X-No-Auth: 1")
    @GET("v9/checkInfo")
    suspend fun checkInfo(): Map<String, Any?>

    // --- Catalogue ---

    @Headers("X-No-Auth: 1")
    @GET("v9/sites")
    suspend fun getSites(): Map<String, List<SiteInfo>>

    @Headers("X-No-Auth: 1")
    @GET("v9/categories")
    suspend fun getCategories(@Query("site") site: String): SiteCategories

    @Headers("X-No-Auth: 1")
    @GET("v9/starter")
    suspend fun getStarter(@Query("site") site: String): StarterMethod

    @Headers("X-No-Auth: 1")
    @POST("v9/sites/{sitetag}/info")
    suspend fun getSiteInfo(@Body request: SiteInfoRequest, @Path("sitetag") siteTag: String): SiteInformation

    // --- Content & stream resolution ---

    @POST("v9/sites/{sitetag}/data")
    suspend fun getData(
        @Body data: SiteData,
        @Path("sitetag") siteTag: String,
        @Query("isTV") isTv: Boolean,
    ): List<VideoInformation>

    @POST("v9/sites/{sitetag}/related")
    suspend fun getRelated(
        @Body data: SiteData,
        @Path("sitetag") siteTag: String,
        @Query("isTV") isTv: Boolean,
    ): List<VideoInformation>

    @POST("v9/sites/{sitetag}/link")
    suspend fun getLink(@Body request: SiteInfoRequest, @Path("sitetag") siteTag: String): LinkResponse

    @POST("v9/sites/{sitetag}/stream")
    suspend fun getStream(
        @Body data: StreamData,
        @Path("sitetag") siteTag: String,
        @Query("isTV") isTv: Boolean,
    ): List<com.streamdev.aiostreamer.baseline.model.VideoLink>

    @POST("v9/sites/{sitetag}/tags")
    suspend fun getTags(
        @Body data: TagData,
        @Path("sitetag") siteTag: String,
        @Query("isTV") isTv: Boolean,
    ): TagResult

    @POST("v9/sites/{sitetag}/extra")
    suspend fun getExtra(@Path("sitetag") siteTag: String, @Query("isTV") isTv: Boolean): ExtraInfoParser

    @Headers("X-No-Auth: 1")
    @POST("v9/videoheaders")
    suspend fun getVideoHeaders(@Body video: VideoObject): VideoHeaders

    @GET("v9/video/{video_id}/info")
    suspend fun getVideoInfo(@Path("video_id") videoId: Int): VideoInformation

    @Headers("X-No-Auth: 1")
    @POST("v9/video/{video_id}/info")
    suspend fun reportVideoInfo(@Path("video_id") videoId: Int, @Body video: VideoObject): SimpleResult

    @POST("v9/porndb")
    suspend fun pornDb(@Body filter: PornDBFilter): List<VideoInformation>

    // --- Favorites ---

    @GET("v9/favorites")
    suspend fun getFavorites(
        @Query("order") order: String,
        @Query("site") site: String,
        @Query("search") search: String,
        @Query("page") page: Int,
        @Query("playlist") playlist: Int,
        @Query("seed") seed: Int,
    ): List<VideoInformation>

    @POST("v9/favorites")
    suspend fun addFavorite(@Body video: VideoInformation): FavoriteResult

    @DELETE("v9/favorites")
    suspend fun deleteFavorites(@Query("fav_ids[]") favIds: List<Int>): SimpleResult

    @GET("v9/favorites/{video_id}")
    suspend fun checkFavorite(@Path("video_id") videoId: Int, @Query("link") link: String): FavoriteResult

    @GET("v9/favorites/sites")
    suspend fun getFavoritesSites(): List<CloudSiteList>

    // --- Playlists ---

    @GET("v9/playlists")
    suspend fun getPlaylists(): List<CloudPlaylist>

    @POST("v9/playlists")
    suspend fun createPlaylist(@Query("playlist_name") playlistName: String): SimpleResult

    @POST("v9/playlists/favorites")
    suspend fun addVideoToNewPlaylist(
        @Body video: VideoInformation,
        @Query("playlist_name") playlistName: String,
    ): SimpleResult

    @PUT("v9/playlists/{playlistId}")
    suspend fun addVideoToPlaylist(@Body video: VideoInformation, @Path("playlistId") playlistId: Int): SimpleResult

    @PATCH("v9/playlists/{playlistId}")
    suspend fun renamePlaylist(@Path("playlistId") playlistId: Int, @Query("playlist_name") playlistName: String): SimpleResult

    @DELETE("v9/playlists/{playlistId}")
    suspend fun deletePlaylist(@Path("playlistId") playlistId: Int): SimpleResult

    @DELETE("v9/playlists/{playlistId}/favorites")
    suspend fun deleteVideosFromPlaylist(
        @Path("playlistId") playlistId: Int,
        @Query("fav_ids[]") favIds: List<Int>,
    ): SimpleResult

    // --- History ---

    @GET("v9/history")
    suspend fun getHistory(
        @Query("order") order: String,
        @Query("site") site: String,
        @Query("search") search: String,
        @Query("page") page: Int,
    ): List<VideoInformation>

    @POST("v9/history")
    suspend fun addHistory(@Body video: VideoInformation): SimpleResult

    @DELETE("v9/history")
    suspend fun deleteHistory(): SimpleResult

    @GET("v9/history/sites")
    suspend fun getHistorySites(): List<CloudSiteList>

    // --- Coins economy ---

    @FormUrlEncoded
    @POST("v9/coins/check")
    suspend fun coinsCheck(@Field("m3u8id") m3u8Id: String): ConversionResponse

    @FormUrlEncoded
    @POST("v9/coins/exchange")
    suspend fun coinsExchange(@Field("m3u8id") m3u8Id: String): SimpleResult

    @FormUrlEncoded
    @POST("v9/coins/id")
    suspend fun coinsId(@Field("m3u8id") m3u8Id: String): SimpleResult

    // --- Cast / diagnostics ---

    @POST("v9/tv/send")
    suspend fun sendToTv(@Body video: VideoToTv): SimpleResult

    @POST("v9/error")
    suspend fun sendError(@Body error: ClientError): ErrorResponse

    @GET("v9/errors")
    suspend fun getErrors(
        @Query("order") order: String,
        @Query("site") site: String,
        @Query("search") search: String,
        @Query("page") page: Int,
    ): List<VideoInformation>

    // --- Utility (non-versioned; no bearer) ---

    @Headers("X-No-Auth: 1")
    @GET("update")
    suspend fun checkUpdate(): UpdateResponse

    @Headers("X-No-Auth: 1")
    @GET("addDownloadCount")
    suspend fun addDownloadCount(@Query("sid") sid: String): Response<Unit>

    @Headers("X-No-Auth: 1")
    @Streaming
    @GET
    suspend fun download(@Url url: String): ResponseBody
}
