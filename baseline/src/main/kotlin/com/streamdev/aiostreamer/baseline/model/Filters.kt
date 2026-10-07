package com.streamdev.aiostreamer.baseline.model

/**
 * Builders and constants for the list/search filters, replacing the scattered
 * filter construction in the original `SetupFilters`. The `viewer` values are the
 * ordering keys the v6.7.1 UI uses (`alpha/new/old/longest/random`), plus the
 * section selectors (`hot`, `mv`).
 */
object Filters {

    const val VIEWER_NEW = "new"
    const val VIEWER_HOT = "hot"
    const val VIEWER_MOST_VIEWED = "mv"
    const val VIEWER_ALPHA = "alpha"
    const val VIEWER_OLD = "old"
    const val VIEWER_LONGEST = "longest"
    const val VIEWER_RANDOM = "random"

    /** A standard listing/search filter for a given page. */
    fun standard(
        viewer: String = VIEWER_NEW,
        page: Int = 1,
        category: Boolean = false,
    ): StandardFilter = StandardFilter(viewer = viewer, page = page.coerceAtLeast(1), category = category)

    /** A PornDB metadata-search filter. */
    fun pornDb(
        page: Int = 1,
        order: String = VIEWER_NEW,
        duration: String = "all",
        fulltext: String? = null,
        pornstars: List<String> = emptyList(),
        studios: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        sites: List<String> = emptyList(),
    ): PornDBFilter = PornDBFilter(
        page = page.coerceAtLeast(1),
        order = order,
        duration = duration,
        fulltext = fulltext,
        pornstars = pornstars,
        studios = studios,
        tags = tags,
        sites = sites,
    )
}
