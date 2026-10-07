package com.streamdev.aiostreamer.core.net

import com.streamdev.aiostreamer.baseline.model.LoginStatus
import com.streamdev.aiostreamer.baseline.net.CredentialStore
import com.streamdev.aiostreamer.core.prefs.SharedPref

/**
 * [CredentialStore] backed by [SharedPref] (key `accessToken`, preserved from the
 * original build). Also persists the last login snapshot (pro/unixtime/status/
 * user_id), which the v9 request hash embeds — see
 * [com.streamdev.aiostreamer.core.security.AndroidHashSigner].
 */
class PrefsCredentialStore(private val prefs: SharedPref) : CredentialStore {

    override fun accessToken(): String = prefs.getString(KEY_TOKEN, "")

    override fun updateAccessToken(token: String) = prefs.put(KEY_TOKEN, token)

    override fun clear() {
        prefs.put(KEY_TOKEN, "")
        prefs.put(KEY_USERNAME, "")
        prefs.put(KEY_PASSWORD, "")
        prefs.put(KEY_PRO, 0)
        prefs.put(KEY_UNIXTIME, 0)
        prefs.put(KEY_STATUS, 0)
        prefs.put(KEY_USER_ID, 0)
    }

    /** Persist the full login snapshot after a successful `v9/login`. */
    fun saveLoginStatus(status: LoginStatus) {
        status.token?.let { prefs.put(KEY_TOKEN, it) }
        prefs.put(KEY_PRO, status.pro.toInt())
        prefs.put(KEY_UNIXTIME, status.unixtime.toInt())
        prefs.put(KEY_STATUS, status.status)
        prefs.put(KEY_USER_ID, status.userId)
    }

    /** Current login snapshot (defaults model the "not logged in" case). */
    fun readLoginStatus(): LoginStatus = LoginStatus(
        pro = prefs.getInt(KEY_PRO, 0).toLong(),
        status = prefs.getInt(KEY_STATUS, 0),
        token = prefs.getString(KEY_TOKEN, ""),
        unixtime = prefs.getInt(KEY_UNIXTIME, 0).toLong(),
        userId = prefs.getInt(KEY_USER_ID, 0),
    )

    private companion object {
        const val KEY_TOKEN = "accessToken"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
        const val KEY_PRO = "pro"
        const val KEY_UNIXTIME = "unixtime"
        const val KEY_STATUS = "loginStatusCode"
        const val KEY_USER_ID = "user_id"
    }
}
