package com.streamdev.aiostreamer.app

import android.content.Context
import com.streamdev.aiostreamer.BuildConfig
import com.streamdev.aiostreamer.baseline.ApiService
import com.streamdev.aiostreamer.baseline.StreamerApi
import com.streamdev.aiostreamer.baseline.StreamerRepository
import com.streamdev.aiostreamer.baseline.net.CredentialStore
import com.streamdev.aiostreamer.baseline.sites.SiteConnectionClient
import com.streamdev.aiostreamer.core.net.PrefsCookieProvider
import com.streamdev.aiostreamer.core.net.PrefsCredentialStore
import com.streamdev.aiostreamer.core.prefs.SharedPref
import com.streamdev.aiostreamer.core.security.AndroidHashSigner

/**
 * Minimal manual dependency graph.
 *
 * Wires the Android platform pieces into the platform-agnostic [StreamerApi] /
 * [StreamerRepository] baseline. Kept as explicit construction (no DI framework)
 * so the single external-connection entry point is obvious and easy to audit.
 */
class Graph(context: Context) {

    private val appContext = context.applicationContext
    val prefs: SharedPref = SharedPref.init(appContext)

    val credentials: CredentialStore = PrefsCredentialStore(prefs)

    val api: ApiService = StreamerApi.create(
        hashSigner = AndroidHashSigner.create(appContext),
        credentials = credentials,
        enableLogging = BuildConfig.DEBUG,
    )

    val repository: StreamerRepository = StreamerRepository(api, credentials)

    val siteConnectionClient: SiteConnectionClient =
        SiteConnectionClient(PrefsCookieProvider(prefs))
}
