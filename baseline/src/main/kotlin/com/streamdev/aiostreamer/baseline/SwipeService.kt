package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.model.GifResponse
import com.streamdev.aiostreamer.baseline.model.RedgifsAuth
import com.streamdev.aiostreamer.baseline.model.SwipeCategory
import com.streamdev.aiostreamer.baseline.model.SwipeItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

/**
 * NSFW swipe-feed backend (`nsfwswipe.com/api/`). Independent of the main
 * backend's auth; no `hash`/bearer headers.
 */
interface SwipeService {

    @GET("getCategories")
    suspend fun getCategories(): List<SwipeCategory>

    @GET("getVideos/{id}/{orientation}")
    suspend fun getVideos(@Path("id") id: String, @Path("orientation") orientation: String): List<SwipeItem>

    @GET("like/{id}")
    suspend fun like(@Path("id") id: Long): Response<Unit>

    @GET("increase/{id}")
    suspend fun increase(@Path("id") id: Long): Response<Unit>
}

/**
 * RedGifs public API (`api.redgifs.com/v2/`). Uses a temporary bearer token
 * obtained from [getTemporaryAuth]; callers pass it back as the Authorization
 * header on [getGif].
 */
interface RedgifsService {

    @GET("auth/temporary")
    suspend fun getTemporaryAuth(@Header("User-Agent") userAgent: String): RedgifsAuth

    @GET
    suspend fun getGif(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("Referer") referer: String,
        @Header("User-Agent") userAgent: String,
    ): GifResponse
}
