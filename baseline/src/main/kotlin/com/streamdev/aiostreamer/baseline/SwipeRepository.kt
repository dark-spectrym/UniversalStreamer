package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.model.GifResponse
import com.streamdev.aiostreamer.baseline.model.SwipeCategory
import com.streamdev.aiostreamer.baseline.model.SwipeItem
import com.streamdev.aiostreamer.baseline.model.SwipeOrientation

/**
 * Coroutine gateway for the swipe feed ([SwipeService], `nsfwswipe.com`) and the
 * RedGifs integration ([RedgifsService], `api.redgifs.com`).
 *
 * RedGifs requires a temporary bearer token before any gif request; [redgifsGif]
 * fetches one lazily (cached for the instance) and retries once on auth expiry.
 */
class SwipeRepository(
    private val swipe: SwipeService,
    private val redgifs: RedgifsService,
    private val userAgent: String,
) {

    @Volatile private var redgifsToken: String? = null

    suspend fun categories(): Result<List<SwipeCategory>> = runCatching { swipe.getCategories() }

    suspend fun videos(subreddit: String, orientation: SwipeOrientation): Result<List<SwipeItem>> =
        runCatching { swipe.getVideos(subreddit, orientation.value) }

    suspend fun like(id: Long): Result<Unit> = runCatching { swipe.like(id); Unit }

    suspend fun recordView(id: Long): Result<Unit> = runCatching { swipe.increase(id); Unit }

    /** Fetches a RedGifs gif, acquiring/refreshing the temporary token as needed. */
    suspend fun redgifsGif(url: String, referer: String = "https://www.redgifs.com/"): Result<GifResponse> =
        runCatching {
            try {
                redgifs.getGif(url, bearer(ensureToken()), referer, userAgent)
            } catch (e: Exception) {
                // token likely expired; refresh once and retry
                redgifsToken = null
                redgifs.getGif(url, bearer(ensureToken()), referer, userAgent)
            }
        }

    private suspend fun ensureToken(): String =
        redgifsToken ?: redgifs.getTemporaryAuth(userAgent).token.orEmpty().also { redgifsToken = it }

    private fun bearer(token: String) = "Bearer $token"
}
