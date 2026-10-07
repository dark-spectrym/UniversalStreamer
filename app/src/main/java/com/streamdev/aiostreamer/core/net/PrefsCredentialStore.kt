package com.streamdev.aiostreamer.core.net

import com.streamdev.aiostreamer.baseline.net.CredentialStore
import com.streamdev.aiostreamer.core.prefs.SharedPref

/**
 * [CredentialStore] backed by [SharedPref], using the original `accessToken` key
 * so existing logged-in users are not signed out by the upgrade.
 */
class PrefsCredentialStore(private val prefs: SharedPref) : CredentialStore {

    override fun accessToken(): String = prefs.getString(KEY_TOKEN, "")

    override fun updateAccessToken(token: String) = prefs.put(KEY_TOKEN, token)

    override fun clear() {
        prefs.put(KEY_TOKEN, "")
        prefs.put(KEY_USERNAME, "")
        prefs.put(KEY_PASSWORD, "")
    }

    private companion object {
        const val KEY_TOKEN = "accessToken"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
    }
}
