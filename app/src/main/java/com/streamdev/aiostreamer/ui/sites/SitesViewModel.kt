package com.streamdev.aiostreamer.ui.sites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.streamdev.aiostreamer.baseline.StreamerRepository
import com.streamdev.aiostreamer.baseline.model.SiteInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the site catalogue screen. */
data class SitesUiState(
    val loading: Boolean = true,
    val groups: List<SiteGroup> = emptyList(),
    val error: String? = null,
) {
    data class SiteGroup(val name: String, val sites: List<SiteInfo>)
}

/**
 * Loads the site catalogue (`v9/sites`) through [StreamerRepository] and exposes it
 * as UI state. Groups are the backend's own keys (e.g. mobile / tv / gay buckets).
 */
class SitesViewModel(private val repository: StreamerRepository) : ViewModel() {

    private val _state = MutableStateFlow(SitesUiState())
    val state: StateFlow<SitesUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            repository.sites()
                .onSuccess { map ->
                    val groups = map.entries
                        .map { SitesUiState.SiteGroup(it.key, it.value) }
                        .sortedByDescending { it.sites.size }
                    _state.value = SitesUiState(loading = false, groups = groups)
                }
                .onFailure { e ->
                    _state.value = SitesUiState(loading = false, error = e.message ?: "Failed to load sites")
                }
        }
    }

    /** Factory so the activity can inject the repository from its dependency graph. */
    class Factory(private val repository: StreamerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SitesViewModel(repository) as T
    }
}
