package com.streamdev.aiostreamer.ui

import com.streamdev.aiostreamer.baseline.model.VideoInformation

/**
 * Lightweight hand-off for the item the user tapped in a listing, read by the
 * player route. Avoids making the DTO Parcelable / threading it through nav args
 * for this single-activity alpha.
 */
object PlaybackSelection {
    @Volatile var siteTag: String = ""
    @Volatile var video: VideoInformation? = null
}
