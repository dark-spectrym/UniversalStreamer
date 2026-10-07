package com.streamdev.aiostreamer.baseline.net

import com.streamdev.aiostreamer.baseline.security.HashSigner
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Injects the two authentication headers on every outgoing backend request:
 *
 *  - `hash`          — freshly computed per request by the [HashSigner]
 *  - `Authorization` — `Bearer <accessToken>` from the [CredentialStore]
 *
 * This removes the `@Header("hash")` / `@Header("Authorization")` parameters that
 * were repeated on all ~30 endpoints in the original `ApiService`, so endpoint
 * signatures describe only their real inputs.
 *
 * An endpoint can opt out of the bearer header (e.g. pre-login calls) by tagging
 * its request with [NO_AUTH]; the `hash` header is always sent.
 */
class AuthInterceptor(
    private val hashSigner: HashSigner,
    private val credentials: CredentialStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("hash", hashSigner.sign())

        val skipAuth = original.header(NO_AUTH) != null
        if (skipAuth) {
            builder.removeHeader(NO_AUTH)
        } else {
            builder.header("Authorization", "Bearer " + credentials.accessToken())
        }
        return chain.proceed(builder.build())
    }

    companion object {
        /** Marker header name; present on a request means "do not attach the bearer token". */
        const val NO_AUTH = "X-No-Auth"
    }
}
