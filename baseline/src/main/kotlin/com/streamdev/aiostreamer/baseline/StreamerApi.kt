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
 * Builds a configured [ApiService]. Replaces the original singleton
 * `RetrofitClient` with an explicit, testable factory:
 *
 *  - the base URL is injectable (point at staging in tests),
 *  - auth headers are centralised in [AuthInterceptor],
 *  - an optional logging interceptor can be attached.
 */
object StreamerApi {

    fun create(
        hashSigner: HashSigner,
        credentials: CredentialStore = InMemoryCredentialStore(),
        baseUrl: String = BaselineConfig.DEFAULT_BASE_URL,
        enableLogging: Boolean = false,
    ): ApiService = buildRetrofit(hashSigner, credentials, baseUrl, enableLogging).create(ApiService::class.java)

    /** Exposed for callers that also need the underlying [Retrofit] (e.g. to add converters). */
    fun buildRetrofit(
        hashSigner: HashSigner,
        credentials: CredentialStore,
        baseUrl: String,
        enableLogging: Boolean,
    ): Retrofit {
        val timeout = BaselineConfig.TIMEOUT_SECONDS
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(hashSigner, credentials))

        if (enableLogging) {
            clientBuilder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
