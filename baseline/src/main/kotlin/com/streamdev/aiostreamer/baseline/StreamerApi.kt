package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.net.AuthInterceptor
import com.streamdev.aiostreamer.baseline.net.CredentialStore
import com.streamdev.aiostreamer.baseline.net.InMemoryCredentialStore
import com.streamdev.aiostreamer.baseline.security.HashSigner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the typed service clients. Replaces the v6.x singleton `RetrofitClient`
 * objects (one per backend) with one explicit, testable factory.
 *
 *  - [create] -> main v9 backend with the [AuthInterceptor] attaching `hash` + bearer.
 *  - [createSwipe] / [createRedgifs] -> the auxiliary feeds, which use no app auth.
 */
object StreamerApi {

    fun create(
        hashSigner: HashSigner,
        credentials: CredentialStore = InMemoryCredentialStore(),
        baseUrl: String = BaselineConfig.DEFAULT_BASE_URL,
        enableLogging: Boolean = false,
    ): ApiService {
        val client = baseClient(enableLogging)
            .addInterceptor(AuthInterceptor(hashSigner, credentials))
            .build()
        return retrofit(baseUrl, client).create(ApiService::class.java)
    }

    fun createSwipe(baseUrl: String = BaselineConfig.SWIPE_BASE_URL, enableLogging: Boolean = false): SwipeService =
        retrofit(baseUrl, baseClient(enableLogging).build()).create(SwipeService::class.java)

    fun createRedgifs(baseUrl: String = BaselineConfig.REDGIFS_BASE_URL, enableLogging: Boolean = false): RedgifsService =
        retrofit(baseUrl, baseClient(enableLogging).build()).create(RedgifsService::class.java)

    private fun baseClient(enableLogging: Boolean): OkHttpClient.Builder {
        val timeout = BaselineConfig.TIMEOUT_SECONDS
        val builder = OkHttpClient.Builder()
            .connectTimeout(BaselineConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
        if (enableLogging) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
        }
        return builder
    }

    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
