package com.streamdev.aiostreamer.baseline.sites

import com.streamdev.aiostreamer.baseline.BaselineConfig
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Centralised client for the app's *direct* connections to external sites.
 *
 * In v6.4.5 these requests were open-coded with jsoup throughout `GetDataRows`,
 * `GetStream` and the per-site fragments, each repeating user-agent strings,
 * age-gate cookies and premium-cookie handling. This client consolidates that
 * into one configurable entry point so every outbound site connection shares the
 * same policy and can be audited in one place.
 *
 * It is deliberately free of Android types; stored per-site cookies are supplied
 * through [CookieProvider], which the Android app implements over its cookie store.
 */
class SiteConnectionClient(
    private val cookieProvider: CookieProvider = CookieProvider.EMPTY,
) {

    /** Device-agnostic source of per-site cookies (e.g. premium login cookies). */
    fun interface CookieProvider {
        /** @return a raw `Cookie:` header value for [siteTag], or empty if none. */
        fun cookieHeader(siteTag: String): String

        companion object {
            val EMPTY = CookieProvider { "" }
        }
    }

    enum class Agent { MOBILE, DESKTOP }

    /** Describes one outbound site request. */
    data class SiteRequest(
        val url: String,
        val siteTag: String = "",
        val agent: Agent = Agent.DESKTOP,
        val referrer: String? = null,
        /** Standard age-gate / consent cookies many tube sites require. */
        val ageGateCookies: Boolean = false,
        val timeoutSeconds: Long = BaselineConfig.SCRAPE_TIMEOUT_SECONDS,
        val followRedirects: Boolean = true,
    )

    /** Fetches [request] and returns the raw response body. */
    fun fetchBody(request: SiteRequest): String = build(request).execute().body()

    /** Fetches [request] and returns the parsed DOM. */
    fun fetchDocument(request: SiteRequest): Document = build(request).get()

    private fun build(request: SiteRequest): Connection {
        val connection = Jsoup.connect(request.url)
            .userAgent(agentString(request.agent))
            .timeout((request.timeoutSeconds * 1000L).toInt())
            .followRedirects(request.followRedirects)
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .method(Connection.Method.GET)

        request.referrer?.let(connection::referrer)

        if (request.ageGateCookies) {
            connection.cookie("accessAgeDisclaimerPH", "1")
                .cookie("cookiesBannerSeen", "1")
                .cookie("hasVisited", "1")
        }

        val stored = request.siteTag.takeIf { it.isNotEmpty() }?.let(cookieProvider::cookieHeader).orEmpty()
        if (stored.isNotEmpty()) {
            connection.header("Cookie", stored)
        }
        return connection
    }

    private fun agentString(agent: Agent): String = when (agent) {
        Agent.MOBILE -> BaselineConfig.USER_AGENT_MOBILE
        Agent.DESKTOP -> BaselineConfig.USER_AGENT_DESKTOP
    }
}
