package com.streamdev.aiostreamer.baseline.sites

import com.streamdev.aiostreamer.baseline.ApiService
import com.streamdev.aiostreamer.baseline.model.SiteData
import com.streamdev.aiostreamer.baseline.model.StreamData
import com.streamdev.aiostreamer.baseline.model.TagData
import com.streamdev.aiostreamer.baseline.model.TagResult
import com.streamdev.aiostreamer.baseline.model.VideoHeaders
import com.streamdev.aiostreamer.baseline.model.VideoInformation
import com.streamdev.aiostreamer.baseline.model.VideoLink
import com.streamdev.aiostreamer.baseline.model.VideoObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orchestrates the two-step "connect to a site" flow the app uses for every
 * listing and stream, reconstructed from the v6.7.1 data source (`jm1`/`ct5`):
 *
 *  1. the client fetches the site's page HTML directly ([SiteConnectionClient]),
 *  2. it POSTs that HTML as the `payload` (plus the page link) to the backend,
 *     which parses it with the server-side site recipe and returns structured data.
 *
 * This keeps the scraping on-device (so the backend never has to fetch the site)
 * while the fragile parsing stays server-side and updatable without an app release.
 *
 * The blocking jsoup fetch runs on [Dispatchers.IO]; the backend calls are the
 * suspend [ApiService] functions.
 */
class SiteContentResolver(
    private val api: ApiService,
    private val siteClient: SiteConnectionClient,
) {

    /** A resolved, playable video: its stream variants plus the playback headers. */
    data class ResolvedStream(
        val links: List<VideoLink>,
        val headers: VideoHeaders,
    )

    /** Loads a listing page: fetch its HTML, then have the backend parse it. */
    suspend fun listing(
        siteTag: String,
        pageUrl: String,
        isTv: Boolean = false,
        agent: SiteConnectionClient.Agent = SiteConnectionClient.Agent.DESKTOP,
        ageGate: Boolean = false,
    ): List<VideoInformation> {
        val html = fetch(pageUrl, siteTag, agent, ageGate)
        return api.getData(SiteData(link = pageUrl, payload = html), siteTag, isTv)
    }

    /** Loads the related-videos list for a watch page. */
    suspend fun related(
        siteTag: String,
        pageUrl: String,
        isTv: Boolean = false,
        agent: SiteConnectionClient.Agent = SiteConnectionClient.Agent.DESKTOP,
        ageGate: Boolean = false,
    ): List<VideoInformation> {
        val html = fetch(pageUrl, siteTag, agent, ageGate)
        return api.getRelated(SiteData(link = pageUrl, payload = html), siteTag, isTv)
    }

    /**
     * Resolves a chosen video to playable streams: fetch the watch-page HTML, have
     * the backend extract the stream variants from it, then fetch the per-stream
     * playback headers.
     */
    suspend fun stream(
        siteTag: String,
        videoPageUrl: String,
        video: VideoObject,
        isTv: Boolean = false,
        agent: SiteConnectionClient.Agent = SiteConnectionClient.Agent.DESKTOP,
        ageGate: Boolean = false,
    ): ResolvedStream {
        val html = fetch(videoPageUrl, siteTag, agent, ageGate)
        val links = api.getStream(StreamData(payload = html, videoObject = video), siteTag, isTv)
        val headers = api.getVideoHeaders(video)
        return ResolvedStream(links, headers)
    }

    /** Scrapes metadata tags (pornstars/studios/tags) for a watch page. */
    suspend fun tags(
        siteTag: String,
        videoPageUrl: String,
        isTv: Boolean = false,
        agent: SiteConnectionClient.Agent = SiteConnectionClient.Agent.DESKTOP,
        ageGate: Boolean = false,
    ): TagResult {
        val html = fetch(videoPageUrl, siteTag, agent, ageGate)
        return api.getTags(TagData(link = videoPageUrl, payload = html), siteTag, isTv)
    }

    private suspend fun fetch(
        url: String,
        siteTag: String,
        agent: SiteConnectionClient.Agent,
        ageGate: Boolean,
    ): String = withContext(Dispatchers.IO) {
        siteClient.fetchBody(
            SiteConnectionClient.SiteRequest(
                url = url,
                siteTag = siteTag,
                agent = agent,
                ageGateCookies = ageGate,
            ),
        )
    }
}
