package com.streamdev.aiostreamer.ui.sites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.streamdev.aiostreamer.baseline.model.SiteInfo

/**
 * Site catalogue screen: a grid of the sites the backend advertises, grouped by
 * the backend's own buckets. Tapping a site hands its tag back to the host.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SitesScreen(
    viewModel: SitesViewModel,
    onSiteSelected: (SiteInfo) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("AIO Streamer") }) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.loading -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text("Loading sites…", style = MaterialTheme.typography.bodyMedium)
                }
                state.error != null -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Could not reach the backend", style = MaterialTheme.typography.titleMedium)
                    Text(state.error!!, style = MaterialTheme.typography.bodySmall)
                    val retryFocus = remember { FocusRequester() }
                    LaunchedEffect(Unit) { runCatching { retryFocus.requestFocus() } }
                    Button(onClick = viewModel::refresh, modifier = Modifier.focusRequester(retryFocus)) {
                        Text("Retry")
                    }
                }
                else -> SiteGrid(state.groups, onSiteSelected)
            }
        }
    }
}

@Composable
private fun SiteGrid(
    groups: List<SitesUiState.SiteGroup>,
    onSiteSelected: (SiteInfo) -> Unit,
) {
    val sites = groups.flatMap { it.sites }.filter { it.online }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 108.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(sites, key = { it.sitetag ?: it.name ?: it.siteId.toString() }) { site ->
            SiteTile(site, onSiteSelected)
        }
    }
}

@Composable
private fun SiteTile(site: SiteInfo, onSiteSelected: (SiteInfo) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onSiteSelected(site) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AsyncImage(
            model = site.image,
            contentDescription = site.name,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
        )
        Text(
            text = site.name ?: site.sitetag.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
