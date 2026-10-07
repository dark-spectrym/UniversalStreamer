package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.model.VideoObject
import com.streamdev.aiostreamer.baseline.net.InMemoryCredentialStore
import com.streamdev.aiostreamer.baseline.security.HashSigner
import com.streamdev.aiostreamer.baseline.sites.SiteConnectionClient
import com.streamdev.aiostreamer.baseline.sites.SiteContentResolver
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies the two-step fetch-then-parse orchestration: the scraped page HTML is
 * sent to the backend as the request payload, and the parsed result is returned.
 */
class SiteContentResolverTest {

    private lateinit var server: MockWebServer
    private lateinit var resolver: SiteContentResolver

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val api = StreamerApi.create(
            hashSigner = HashSigner { "h" },
            credentials = InMemoryCredentialStore("tok"),
            baseUrl = server.url("/api/").toString(),
        )
        resolver = SiteContentResolver(api, SiteConnectionClient())
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun listing_sendsScrapedHtmlAsPayload_andParsesResult() = runBlocking {
        // 1) the direct site fetch (jsoup) -> page HTML
        server.enqueue(MockResponse().setBody("<html><body>PAGE-MARKER</body></html>"))
        // 2) the backend parse of that HTML -> structured videos
        server.enqueue(MockResponse().setBody("""[{"video_id":11,"title":"v","link":"watch"}]"""))

        val pageUrl = server.url("/listing/new/1").toString()
        val videos = resolver.listing("examplecom", pageUrl, isTv = false)

        assertEquals(1, videos.size)
        assertEquals(11, videos[0].videoId)

        val fetch = server.takeRequest()
        assertEquals("GET", fetch.method)
        assertEquals("/listing/new/1", fetch.path)

        val parse = server.takeRequest()
        assertEquals("POST", parse.method)
        assertEquals("/api/v9/sites/examplecom/data?isTV=false", parse.path)
        val body = parse.body.readUtf8()
        assertTrue("payload carries scraped HTML", body.contains("PAGE-MARKER"))
        assertTrue("payload carries page link", body.contains("/listing/new/1"))
    }

    @Test
    fun openListing_fetchesRecipeThenPageThenParses() = runBlocking {
        // 1) getSiteInfo returns the recipe with the page-baked URL
        val pageUrl = server.url("/new/2").toString()
        server.enqueue(MockResponse().setBody("""{"newUrl":"$pageUrl","sitetag":"examplecom"}"""))
        // 2) the direct site fetch of that URL -> HTML
        server.enqueue(MockResponse().setBody("<html>PAGE2</html>"))
        // 3) backend parse -> videos
        server.enqueue(MockResponse().setBody("""[{"video_id":21,"title":"v2"}]"""))

        val videos = resolver.openListing(
            "examplecom",
            com.streamdev.aiostreamer.baseline.model.Filters.standard(page = 2),
        )

        assertEquals(1, videos.size)
        assertEquals(21, videos[0].videoId)
        assertEquals("/api/v9/sites/examplecom/info", server.takeRequest().path) // getSiteInfo
        server.takeRequest()                                                     // jsoup fetch of pageUrl
        assertEquals("/api/v9/sites/examplecom/data?isTV=false", server.takeRequest().path)
    }

    @Test
    fun stream_resolvesLinksThenHeaders() = runBlocking {
        server.enqueue(MockResponse().setBody("<html>WATCH</html>"))                 // jsoup fetch
        server.enqueue(MockResponse().setBody("""[{"quality":"720p","stream":"https://cdn/x.m3u8","type":"hls"}]"""))
        server.enqueue(MockResponse().setBody("""{"m3u8":true,"useragent":"UA","headers":{"Referer":"r"}}"""))

        val watchUrl = server.url("/watch/1").toString()
        val resolved = resolver.stream("examplecom", watchUrl, VideoObject(videoId = 1, site = "examplecom"))

        assertEquals(1, resolved.links.size)
        assertEquals("720p", resolved.links[0].quality)
        assertTrue(resolved.headers.m3u8)
        assertEquals("UA", resolved.headers.userAgent)

        server.takeRequest() // jsoup
        assertEquals("/api/v9/sites/examplecom/stream?isTV=false", server.takeRequest().path)
        assertEquals("/api/v9/videoheaders", server.takeRequest().path)
    }
}
