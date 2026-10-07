package com.streamdev.aiostreamer.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.streamdev.aiostreamer.app.StreamerApp
import com.streamdev.aiostreamer.ui.sites.SitesScreen
import com.streamdev.aiostreamer.ui.sites.SitesViewModel
import com.streamdev.aiostreamer.ui.theme.StreamerTheme

/**
 * Compose entry point. Hosts the site catalogue, which exercises the full v9
 * networking path (hash signing → auth interceptor → Retrofit → Gson) through the
 * baseline repository. Per-site listing, player, and the other screens from the
 * v6.7.1 inventory (docs/FEATURES.md) are built on this shell.
 */
class MainActivity : ComponentActivity() {

    private val sitesViewModel: SitesViewModel by viewModels {
        SitesViewModel.Factory((application as StreamerApp).graph.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            StreamerTheme {
                SitesScreen(
                    viewModel = sitesViewModel,
                    onSiteSelected = { site ->
                        // Listing screen wiring lands in the next UI slice; confirm selection for now.
                        Toast.makeText(this, site.name ?: site.sitetag, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }
}
