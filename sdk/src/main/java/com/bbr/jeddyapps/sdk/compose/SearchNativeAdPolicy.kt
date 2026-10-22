package com.bbr.jeddyapps.sdk.compose

internal object SearchNativeAdPolicy {
    /** Each ad precedes up to two movies; do not append an ad after the final movie. */
    fun indexes(resultCount: Int): List<Int> {
        if (resultCount < 3) return emptyList()
        val adCount = resultCount / 2 + resultCount % 2
        return List(adCount) { it * 3 }
    }
}
