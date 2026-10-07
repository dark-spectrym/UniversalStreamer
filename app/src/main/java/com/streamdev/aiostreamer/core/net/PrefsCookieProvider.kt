package com.streamdev.aiostreamer.core.net

import com.streamdev.aiostreamer.baseline.sites.SiteConnectionClient
import com.streamdev.aiostreamer.core.prefs.SharedPref

/**
 * Supplies per-site premium-login cookies to the [SiteConnectionClient].
 *
 * Mirrors the original convention where a site's cookies were stored under
 * `"<siteTag>Cookie"` / `"<siteTag>Cookies"` in SharedPreferences after the
 * user logged into that site's WebView.
 */
class PrefsCookieProvider(private val prefs: SharedPref) : SiteConnectionClient.CookieProvider {

    override fun cookieHeader(siteTag: String): String {
        val single = prefs.getString(siteTag + "Cookie", "")
        if (single.isNotEmpty()) return single
        return prefs.getString(siteTag + "Cookies", "")
    }
}
