package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.net.AuthInterceptor
import com.streamdev.aiostreamer.baseline.net.InMemoryCredentialStore
import com.streamdev.aiostreamer.baseline.security.HashSigner
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies the API baseline end-to-end against a mock backend: request shaping,
 * header injection by the interceptor, and JSON (de)serialisation of the DTOs.
 */
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
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun login_parsesStatusAndPersistsToken() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"status":200,"pro":2000000000,"unixtime":1700000000,"token":"new-token"}""",
            ),
        )
        val repo = StreamerRepository(api, credentials)

        val result = repo.login("alice", "deadbeef", "android-xyz")

        assertTrue(result.isSuccess)
        val status = result.getOrThrow()
        assertTrue(status.isSuccess())
        assertTrue(status.isPro())
        assertEquals("new-token", credentials.accessToken())

        val recorded = server.takeRequest()
        assertEquals("/api/v7/login", recorded.path)
        assertEquals("signed-hash", recorded.getHeader("hash"))
        // login is an authenticated endpoint: bearer header present
        assertEquals("Bearer tok-123", recorded.getHeader("Authorization"))
    }

    @Test
    fun noAuthEndpoint_omitsBearerButKeepsHash() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"mobile":[]}"""))

        api.getSites()

        val recorded = server.takeRequest()
        assertEquals("/api/v7/sites", recorded.path)
        assertEquals("signed-hash", recorded.getHeader("hash"))
        assertNull(recorded.getHeader("Authorization"))
        // the opt-out marker header must be stripped before the wire
        assertNull(recorded.getHeader(AuthInterceptor.NO_AUTH))
    }

    @Test
    fun deleteFavorites_encodesRepeatedQueryParam() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"result":"ok","statusCode":200}"""))

        val res = api.deleteFavorites(listOf(1, 2, 3))

        assertEquals(200, res.statusCode)
        val recorded = server.takeRequest()
        assertTrue(recorded.path!!.contains("fav_ids%5B%5D=1"))
        assertTrue(recorded.path!!.contains("fav_ids%5B%5D=2"))
        assertTrue(recorded.path!!.contains("fav_ids%5B%5D=3"))
    }

    @Test
    fun loginStatus_notProWhenExpiryBeforeServerClock() {
        val expired = com.streamdev.aiostreamer.baseline.model.LoginStatus(
            pro = 1000L, status = 200, token = "t", unixtime = 2000L,
        )
        assertFalse(expired.isPro())
    }
}
