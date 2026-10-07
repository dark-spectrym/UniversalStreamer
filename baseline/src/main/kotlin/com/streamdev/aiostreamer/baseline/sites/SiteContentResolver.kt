package com.streamdev.aiostreamer.baseline.sites

import com.streamdev.aiostreamer.baseline.ApiService
import com.streamdev.aiostreamer.baseline.model.Filters
import com.streamdev.aiostreamer.baseline.model.SiteData
import com.streamdev.aiostreamer.baseline.model.SiteInfoRequest
import com.streamdev.aiostreamer.baseline.model.SiteInformation
import com.streamdev.aiostreamer.baseline.model.StandardFilter
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

    /**
     * One call for a site listing: ask the backend for the site's scraping recipe
     * (`getSiteInfo`, which bakes the requested page/viewer into the returned URLs),
     * pick the URL for the current viewer, fetch it, and have the backend parse it.
     * This is the exact sequence the v6.7.1 content screen uses (getInfo → fetch →
     * getData).
     */
    suspend fun openListing(
        siteTag: String,
        filter: StandardFilter,
        globalSearch: Boolean = false,
        pornTabs: Boolean = false,
        isTv: Boolean = false,
        agent: SiteConnectionClient.Agent = SiteConnectionClient.Agent.DESKTOP,
        ageGate: Boolean = true,
    ): List<VideoInformation> {
        val info = api.getSiteInfo(SiteInfoRequest(siteTag, filter, globalSearch, pornTabs), siteTag)
        val url = selectListingUrl(info, filter) ?: return emptyList()
        return listing(siteTag, url, isTv, agent, ageGate)
    }

    /** Picks the page URL the backend returned for the active viewer/ordering. */
    fun selectListingUrl(info: SiteInformation, filter: StandardFilter): String? {
        val chosen = when (filter.viewer) {
            Filters.VIEWER_HOT -> info.hotUrl?.ifBlank { null } ?: info.newUrl
            Filters.VIEWER_MOST_VIEWED -> info.mvUrl?.ifBlank { null } ?: info.newUrl
            else -> info.newUrl // new/alpha/old/longest/random are server-side orderings of the "new" URL
        }
        return chosen?.ifBlank { null }
    }

    /**
     * Resolves a chosen listing item to playable streams: builds the [VideoObject]
     * from the [VideoInformation] and runs the fetch → getStream → videoheaders flow.
     */
    suspend fun resolvePlayable(
        siteTag: String,
        video: VideoInformation,
        isTv: Boolean = false,
        agent: SiteConnectionClient.Agent = SiteConnectionClient.Agent.DESKTOP,
        ageGate: Boolean = true,
    ): ResolvedStream {
        val vo = VideoObject(
            sourceLink = video.link,
            hosterLink = video.link,
            title = video.title,
            image = video.img,
            site = video.site ?: siteTag,
            videoId = video.videoId,
        )
        return stream(siteTag, video.link.orEmpty(), vo, isTv, agent, ageGate)
    }

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
