package com.bbr.jeddyapps.sdk.compose

import com.bbr.jeddyapps.sdk.NativeAd
import androidx.compose.ui.unit.IntSize

sealed interface NativeAdLoadState {
    data object Loading : NativeAdLoadState
    data class Loaded(val ad: NativeAd) : NativeAdLoadState {
        internal val presentation = NativeAdPresentationState()
    }
    data object Failed : NativeAdLoadState
}

/** Stable placement identity shared by the loading placeholder and its eventual ad. */
data class NativeAdSlot(val id: Long, val state: NativeAdLoadState)

/** Presentation metadata follows the loaded resource rather than a disposable lazy row. */
internal class NativeAdPresentationState {
    var hasRevealed = false
        private set
    private var measurement: Measurement? = null

    fun claimReveal(): Boolean {
        if (hasRevealed) return false
        // Claim at the start so scrolling away during the fade cannot replay it later.
        hasRevealed = true
        return true
    }

    fun recordMeasurement(size: IntSize, density: Float, fontScale: Float) {
        if (size.width > 0 && size.height > 0) measurement = Measurement(size, density, fontScale)
    }

    fun measuredHeight(width: Int, density: Float, fontScale: Float): Int? = measurement
        ?.takeIf { it.size.width == width && it.density == density && it.fontScale == fontScale }
        ?.size?.height

    private data class Measurement(val size: IntSize, val density: Float, val fontScale: Float)
}

internal fun buildSearchNativeAdSlots(
    resultCount: Int,
    states: Map<Int, NativeAdLoadState>,
): Map<Int, NativeAdSlot> {
    val indexes = SearchNativeAdPolicy.indexes(resultCount)
    return buildMap {
        var placement = 0
        indexes.indices.forEach { ordinal ->
            // Reserve space before the loading coroutine starts, including queued requests.
            val state = states[ordinal] ?: NativeAdLoadState.Loading
            if (state != NativeAdLoadState.Failed) {
                put(indexes[placement++], NativeAdSlot(ordinal.toLong(), state))
            }
        }
    }
}
