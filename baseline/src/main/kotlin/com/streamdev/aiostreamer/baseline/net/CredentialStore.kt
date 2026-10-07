package com.streamdev.aiostreamer.baseline.net

/**
 * Supplies and persists the bearer token used for authenticated backend calls.
 *
 * The original build read/wrote `accessToken`, `username` and `password` directly
 * from `SharedPref` at every call site. The baseline depends only on this
 * abstraction; the Android app backs it with encrypted shared preferences, and
 * tests back it with [InMemoryCredentialStore].
 */
interface CredentialStore {
    fun accessToken(): String
    fun updateAccessToken(token: String)
    fun clear()
}

/** Simple in-memory [CredentialStore], used in tests and for unauthenticated flows. */
class InMemoryCredentialStore(initialToken: String = "") : CredentialStore {
    @Volatile private var token: String = initialToken
    override fun accessToken(): String = token
    override fun updateAccessToken(token: String) { this.token = token }
    override fun clear() { token = "" }
}
