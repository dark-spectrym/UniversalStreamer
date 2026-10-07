package com.streamdev.aiostreamer.baseline.security

/**
 * Produces the value sent in the `hash` header on every backend request.
 *
 * The backend uses this header to verify the request originates from a genuine,
 * correctly-signed client. The platform-agnostic baseline depends only on this
 * interface; the Android app supplies an implementation backed by its real
 * signing-certificate digest ([RsaHashSigner]).
 */
fun interface HashSigner {
    /** @return a freshly computed, base64 RSA-encrypted hash for the current moment. */
    fun sign(): String

    companion object {
        /** A signer that returns an empty hash. Useful for tests and unauthenticated calls. */
        val NONE: HashSigner = HashSigner { "" }
    }
}
