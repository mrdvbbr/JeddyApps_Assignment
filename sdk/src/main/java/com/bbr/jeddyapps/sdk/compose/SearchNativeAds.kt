package com.bbr.jeddyapps.sdk.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** A null result count retains placements during a transient paging refresh or restoration. */
@Composable
fun rememberSearchNativeAds(
    session: SearchNativeAdsSession,
    resultCount: Int?,
): Map<Int, NativeAdSlot> {
    val inspectionMode = LocalInspectionMode.current
    val states by session.states.collectAsStateWithLifecycle()
    val retainedResultCount by session.resultCount.collectAsStateWithLifecycle()

    LaunchedEffect(session, resultCount, inspectionMode) {
        if (!inspectionMode && resultCount != null) session.updateResultCount(resultCount)
    }

    val displayedResultCount = resultCount ?: retainedResultCount
    return if (inspectionMode) emptyMap() else remember(displayedResultCount, states) {
        buildSearchNativeAdSlots(displayedResultCount, states)
    }
}
