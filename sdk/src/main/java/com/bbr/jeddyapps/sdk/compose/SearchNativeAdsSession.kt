package com.bbr.jeddyapps.sdk.compose

import com.bbr.jeddyapps.sdk.AdProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Owns native ad resources for its caller's scope, independently of screen composition. */
class SearchNativeAdsSession(provider: AdProvider, scope: CoroutineScope) {
    private val mutableResultCount = MutableStateFlow(0)
    val resultCount: StateFlow<Int> = mutableResultCount.asStateFlow()

    private val mutableStates = MutableStateFlow<Map<Int, NativeAdLoadState>>(emptyMap())
    val states: StateFlow<Map<Int, NativeAdLoadState>> = mutableStates.asStateFlow()

    init {
        scope.launch {
            collectSearchNativeAds(
                provider = provider,
                resultCounts = mutableResultCount,
                onSlotsChanged = { mutableStates.value = it },
            )
        }
    }

    fun updateResultCount(resultCount: Int) {
        mutableResultCount.value = resultCount.coerceAtLeast(0)
    }
}
