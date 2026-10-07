package com.streamdev.aiostreamer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.streamdev.aiostreamer.baseline.BaselineConfig
import com.streamdev.aiostreamer.baseline.model.VideoInformation
import com.streamdev.aiostreamer.baseline.sites.SiteContentResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val loading: Boolean = true,
    val title: String? = null,
    val streamUrl: String? = null,
    val userAgent: String = BaselineConfig.USER_AGENT_DESKTOP,
    val headers: Map<String, String> = emptyMap(),
    val error: String? = null,
)

/**
 * Resolves the tapped video to a playable stream via
 * [SiteContentResolver.resolvePlayable] (fetch watch-page HTML → getStream →
 * videoheaders), then exposes the stream URL + playback headers for ExoPlayer.
 */
class PlayerViewModel(private val resolver: SiteContentResolver) : ViewModel() {

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    fun resolve(siteTag: String, video: VideoInformation) {
        _state.value = PlayerUiState(loading = true, title = video.title)
        viewModelScope.launch {
            runCatching { resolver.resolvePlayable(siteTag, video) }
                .onSuccess { resolved ->
                    val url = resolved.links.firstOrNull { !it.streamLink.isNullOrBlank() }?.streamLink
                    if (url.isNullOrBlank()) {
                        _state.value = PlayerUiState(loading = false, title = video.title, error = "No playable stream found")
                    } else {
                        _state.value = PlayerUiState(
                            loading = false,
                            title = video.title,
                            streamUrl = url,
                            userAgent = resolved.headers.userAgent?.ifBlank { null }
                                ?: BaselineConfig.USER_AGENT_DESKTOP,
                            headers = resolved.headers.headers,
                        )
                    }
                }
                .onFailure { e ->
                    _state.value = PlayerUiState(loading = false, title = video.title, error = e.message ?: "Playback failed")
                }
        }
    }

    class Factory(private val resolver: SiteContentResolver) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlayerViewModel(resolver) as T
    }
}
