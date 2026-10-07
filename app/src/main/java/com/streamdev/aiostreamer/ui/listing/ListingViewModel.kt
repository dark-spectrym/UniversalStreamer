package com.streamdev.aiostreamer.ui.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.streamdev.aiostreamer.baseline.model.Filters
import com.streamdev.aiostreamer.baseline.model.VideoInformation
import com.streamdev.aiostreamer.baseline.sites.SiteContentResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListingUiState(
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val videos: List<VideoInformation> = emptyList(),
    val page: Int = 1,
    val canLoadMore: Boolean = true,
    val error: String? = null,
)

/**
 * Loads a site's video listing through [SiteContentResolver.openListing]
 * (getSiteInfo → fetch page HTML → getData) and pages by incrementing the filter.
 */
class ListingViewModel(
    private val resolver: SiteContentResolver,
    private val siteTag: String,
) : ViewModel() {

    private val _state = MutableStateFlow(ListingUiState())
    val state: StateFlow<ListingUiState> = _state.asStateFlow()

    init { load(1, replace = true) }

    fun retry() = load(1, replace = true)

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || !s.canLoadMore) return
        load(s.page + 1, replace = false)
    }

    private fun load(page: Int, replace: Boolean) {
        _state.value = _state.value.copy(
            loading = replace,
            loadingMore = !replace,
            error = if (replace) null else _state.value.error,
        )
        viewModelScope.launch {
            resolver.runCatchingListing(siteTag, page)
                .onSuccess { fresh ->
                    val merged = if (replace) fresh else _state.value.videos + fresh
                    _state.value = ListingUiState(
                        loading = false,
                        loadingMore = false,
                        videos = merged,
                        page = page,
                        canLoadMore = fresh.isNotEmpty(),
                        error = null,
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        loadingMore = false,
                        error = e.message ?: "Failed to load videos",
                    )
                }
        }
    }

    private suspend fun SiteContentResolver.runCatchingListing(siteTag: String, page: Int) =
        runCatching { openListing(siteTag, Filters.standard(page = page)) }

    class Factory(
        private val resolver: SiteContentResolver,
        private val siteTag: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ListingViewModel(resolver, siteTag) as T
    }
}
