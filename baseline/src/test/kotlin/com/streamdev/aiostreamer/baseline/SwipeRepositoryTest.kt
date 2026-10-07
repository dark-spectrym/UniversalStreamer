package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.model.SwipeOrientation
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SwipeRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: SwipeRepository

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val base = server.url("/").toString()
        repo = SwipeRepository(
            swipe = StreamerApi.createSwipe(baseUrl = base),
            redgifs = StreamerApi.createRedgifs(baseUrl = base),
            userAgent = "test-UA",
        )
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun videos_hitsOrientationPath_andParses() = runBlocking {
        server.enqueue(MockResponse().setBody("""[{"swipeId":5,"title":"clip","mp4_url":"https://x/a.mp4"}]"""))

        val items = repo.videos("funny", SwipeOrientation.VERTICAL).getOrThrow()

        assertEquals(1, items.size)
        assertEquals(5L, items[0].swipeId)
        assertEquals("/getVideos/funny/vertical", server.takeRequest().path)
    }

    @Test
    fun redgifsGif_acquiresTokenThenFetchesWithBearer() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"token":"abc123"}"""))                 // auth/temporary
        server.enqueue(MockResponse().setBody("""{"gif":{"urls":{"hd":"https://h","sd":"https://s"}}}"""))

        val gif = repo.redgifsGif(server.url("/v2/gifs/42").toString()).getOrThrow()

        assertEquals("https://h", gif.gif?.urls?.hd)
        assertEquals("/auth/temporary", server.takeRequest().path)
        val gifReq = server.takeRequest()
        assertEquals("Bearer abc123", gifReq.getHeader("Authorization"))
        assertTrue(gifReq.path!!.endsWith("/v2/gifs/42"))
    }
}
