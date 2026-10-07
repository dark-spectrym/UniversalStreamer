package com.streamdev.aiostreamer.ui.listing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.streamdev.aiostreamer.baseline.model.VideoInformation

/**
 * A site's paged video listing. Taps hand the chosen item to the player route via
 * [com.streamdev.aiostreamer.ui.PlaybackSelection].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingScreen(
    title: String,
    viewModel: ListingViewModel,
    onBack: () -> Unit,
    onVideoSelected: (VideoInformation) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    // Infinite scroll: load the next page when the last rows become visible.
    val atEnd by remember {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.videos.size - 4
        }
    }
    LaunchedEffect(atEnd, state.videos.size) {
        if (atEnd && state.videos.isNotEmpty()) viewModel.loadMore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.loading -> CircularProgressIndicator()
                state.error != null && state.videos.isEmpty() -> Text(
                    "Could not load: ${state.error}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(24.dp),
                )
                state.videos.isEmpty() -> Text("No videos found.")
                else -> LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(
                        state.videos,
                        key = { i, v -> "${v.videoId}-${v.link}-$i" },
                    ) { _, video -> VideoCard(video, onVideoSelected) }
                }
            }
        }
    }
}

@Composable
private fun VideoCard(video: VideoInformation, onClick: (VideoInformation) -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable { onClick(video) },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AsyncImage(
            model = video.img,
            contentDescription = video.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
        )
        Text(
            text = video.title.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (!video.duration.isNullOrBlank()) {
            Text(video.duration!!, style = MaterialTheme.typography.labelSmall)
        }
    }
}
