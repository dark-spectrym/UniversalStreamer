package com.streamdev.aiostreamer.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.streamdev.aiostreamer.app.Graph
import com.streamdev.aiostreamer.ui.gate.AgeGateScreen
import com.streamdev.aiostreamer.ui.listing.ListingScreen
import com.streamdev.aiostreamer.ui.listing.ListingViewModel
import com.streamdev.aiostreamer.ui.player.PlayerScreen
import com.streamdev.aiostreamer.ui.player.PlayerViewModel
import com.streamdev.aiostreamer.ui.sites.SitesScreen
import com.streamdev.aiostreamer.ui.sites.SitesViewModel

private const val PREF_OLD_ENOUGH = "oldenough"

/**
 * Single-activity navigation: age gate → site catalogue → per-site listing → player.
 */
@Composable
fun AppNav(graph: Graph, onExit: () -> Unit) {
    val navController = rememberNavController()
    // Always open on the dependency-free entry screen so the app is guaranteed to
    // show an operable first frame (important on Android TV, where landing straight
    // on a network-loading screen with nothing focusable looks like a frozen app).
    NavHost(navController = navController, startDestination = "gate") {
        composable("gate") {
            AgeGateScreen(
                onConfirm = {
                    graph.prefs.put(PREF_OLD_ENOUGH, true)
                    navController.navigate("sites") { popUpTo("gate") { inclusive = true } }
                },
                onDecline = onExit,
            )
        }

        composable("sites") {
            val vm: SitesViewModel = viewModel(factory = SitesViewModel.Factory(graph.repository))
            SitesScreen(
                viewModel = vm,
                onSiteSelected = { site ->
                    val tag = site.sitetag.orEmpty()
                    val name = site.name ?: site.sitetag.orEmpty()
                    if (tag.isNotEmpty()) {
                        navController.navigate("listing/${Uri.encode(tag)}/${Uri.encode(name)}")
                    }
                },
            )
        }

        composable("listing/{siteTag}/{siteName}") { entry ->
            val siteTag = entry.arguments?.getString("siteTag").orEmpty()
            val siteName = entry.arguments?.getString("siteName").orEmpty()
            val vm: ListingViewModel = viewModel(
                key = "listing-$siteTag",
                factory = ListingViewModel.Factory(graph.siteContentResolver, siteTag),
            )
            ListingScreen(
                title = siteName,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onVideoSelected = { video ->
                    PlaybackSelection.siteTag = siteTag
                    PlaybackSelection.video = video
                    navController.navigate("player")
                },
            )
        }

        composable("player") {
            val vm: PlayerViewModel = viewModel(factory = PlayerViewModel.Factory(graph.siteContentResolver))
            val video = PlaybackSelection.video
            androidx.compose.runtime.LaunchedEffect(video) {
                if (video != null) vm.resolve(PlaybackSelection.siteTag, video)
            }
            PlayerScreen(viewModel = vm)
        }
    }
}
