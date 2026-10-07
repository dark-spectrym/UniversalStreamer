package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.net.AuthInterceptor
import com.streamdev.aiostreamer.baseline.net.InMemoryCredentialStore
import com.streamdev.aiostreamer.baseline.security.HashSigner
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** End-to-end checks of the v9 baseline against a mock backend. */
class ApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var credentials: InMemoryCredentialStore
    private lateinit var api: ApiService

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        credentials = InMemoryCredentialStore(initialToken = "tok-123")
        api = StreamerApi.create(
            hashSigner = HashSigner { "signed-hash" },
            credentials = credentials,
            baseUrl = server.url("/api/").toString(),
        )
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun login_parsesStatusAndPersistsToken() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"status":200,"pro":2000000000,"unixtime":1700000000,"token":"new-token","user_id":42}""",
            ),
        )
        val repo = StreamerRepository(api, credentials)

        val status = repo.login("alice", "deadbeef", "android-xyz").getOrThrow()

        assertTrue(status.isSuccess())
        assertTrue(status.isPro())
        assertEquals(42, status.userId)
        assertEquals("new-token", credentials.accessToken())

        val recorded = server.takeRequest()
        assertEquals("/api/v9/login", recorded.path)
        assertEquals("signed-hash", recorded.getHeader("hash"))
        assertEquals("Bearer tok-123", recorded.getHeader("Authorization"))
    }

    @Test
    fun restfulSiteData_hitsVersionedResourcePath() = runBlocking {
        server.enqueue(MockResponse().setBody("""[{"video_id":7,"title":"t","link":"l"}]"""))

        val videos = api.getData(
            com.streamdev.aiostreamer.baseline.model.SiteData(link = "l"),
            siteTag = "examplecom",
            isTv = false,
        )

        assertEquals(1, videos.size)
        assertEquals(7, videos[0].videoId)
        val recorded = server.takeRequest()
        assertEquals("/api/v9/sites/examplecom/data?isTV=false", recorded.path)
        assertEquals("Bearer tok-123", recorded.getHeader("Authorization"))
    }

    @Test
    fun noAuthEndpoint_omitsBearerButKeepsHash() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"mobile":[]}"""))

        api.getSites()

        val recorded = server.takeRequest()
        assertEquals("/api/v9/sites", recorded.path)
        assertEquals("signed-hash", recorded.getHeader("hash"))
        assertNull(recorded.getHeader("Authorization"))
        assertNull(recorded.getHeader(AuthInterceptor.NO_AUTH))
    }

    @Test
    fun deleteFavorites_encodesRepeatedQueryParam() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"result":"ok","statusCode":200}"""))

        val res = api.deleteFavorites(listOf(1, 2, 3))

        assertEquals(200, res.statusCode)
        val recorded = server.takeRequest()
        assertTrue(recorded.path!!.contains("fav_ids%5B%5D=1"))
        assertTrue(recorded.path!!.contains("fav_ids%5B%5D=3"))
    }

    @Test
    fun entryHandshake_deviceAndCheckInfo() = runBlocking {
        val repo = StreamerRepository(api, credentials)
        server.enqueue(MockResponse().setBody("""{"result":"ok","statusCode":200}"""))
        server.enqueue(MockResponse().setBody("""{"maintenance":false}"""))

        assertEquals(200, repo.registerDevice("android-xyz").getOrThrow().statusCode)
        val device = server.takeRequest()
        assertEquals("/api/v9/device", device.path)
        assertTrue(device.body.readUtf8().contains("android_id=android-xyz"))

        val info = repo.checkInfo().getOrThrow()
        assertEquals(false, info["maintenance"])
        assertEquals("/api/v9/checkInfo", server.takeRequest().path)
    }

    @Test
    fun coinsCheck_parsesConversionResponse() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"pro":0,"coins":5,"needed":10}"""))

        val res = api.coinsCheck("m3u8-abc")

        assertEquals(5, res.coins)
        assertEquals(10, res.needed)
        assertEquals("/api/v9/coins/check", server.takeRequest().path)
    }
}
