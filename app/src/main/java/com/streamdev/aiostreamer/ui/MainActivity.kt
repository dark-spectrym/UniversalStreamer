package com.streamdev.aiostreamer.ui

import android.os.Bundle
import android.view.Gravity
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.streamdev.aiostreamer.BuildConfig
import com.streamdev.aiostreamer.app.StreamerApp
import kotlinx.coroutines.launch

/**
 * Shell launcher activity for the revived baseline.
 *
 * It renders the app/version banner and exercises the reconstructed API baseline
 * by calling `v7/sites` through the repository, proving the full networking path
 * (hash signing → auth interceptor → Retrofit → Gson) is wired correctly. The
 * original feature surface (navigation drawer, per-site grids, player, TV UI,
 * downloads, lock screen) is documented in docs/FEATURES.md and is ported onto
 * this baseline incrementally.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val output = TextView(this).apply {
            textSize = 16f
            setPadding(48, 64, 48, 64)
            gravity = Gravity.START
            text = "AIO Streamer ${BuildConfig.VERSION_NAME}\nRevived baseline\n\nContacting backend…"
        }
        setContentView(ScrollView(this).apply { addView(output) })

        val repository = (application as StreamerApp).graph.repository
        lifecycleScope.launch {
            repository.sites()
                .onSuccess { sites ->
                    val total = sites.values.sumOf { it.size }
                    val groups = sites.entries.joinToString("\n") { "  ${it.key}: ${it.value.size}" }
                    output.text = buildString {
                        appendLine("AIO Streamer ${BuildConfig.VERSION_NAME}")
                        appendLine("Revived baseline")
                        appendLine()
                        appendLine("Backend OK — $total sites in ${sites.size} groups:")
                        appendLine(groups)
                    }
                }
                .onFailure { err ->
                    output.text = buildString {
                        appendLine("AIO Streamer ${BuildConfig.VERSION_NAME}")
                        appendLine("Revived baseline")
                        appendLine()
                        appendLine("Backend unreachable: ${err.message}")
                    }
                }
        }
    }
}
