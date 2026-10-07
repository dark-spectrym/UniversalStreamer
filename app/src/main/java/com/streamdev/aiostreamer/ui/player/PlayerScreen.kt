package com.streamdev.aiostreamer.ui.player

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

/**
 * ExoPlayer2 playback surface. Builds an HTTP data source carrying the
 * backend-provided user-agent + headers (cookies, referer) so the CDN accepts the
 * request, then plays the resolved stream (HLS or progressive — the exoplayer-hls
 * module lets DefaultMediaSourceFactory pick by content type).
 */
@Composable
fun PlayerScreen(viewModel: PlayerViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.loading -> CircularProgressIndicator()
            state.error != null -> Text(
                state.error!!,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp),
            )
            state.streamUrl != null -> ExoPlayerSurface(
                url = state.streamUrl!!,
                userAgent = state.userAgent,
                headers = state.headers,
            )
        }
    }
}

@Composable
private fun ExoPlayerSurface(url: String, userAgent: String, headers: Map<String, String>) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val player = androidx.compose.runtime.remember(url) {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .apply { if (headers.isNotEmpty()) setDefaultRequestProperties(headers) }
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(httpFactory))
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(url) {
        onDispose { player.release() }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
        },
    )
}
