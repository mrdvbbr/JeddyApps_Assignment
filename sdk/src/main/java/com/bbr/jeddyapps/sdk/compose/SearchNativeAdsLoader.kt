package com.bbr.jeddyapps.sdk.compose

import com.bbr.jeddyapps.sdk.AdProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Request only additional placements on paging append, with at most two loads in flight. */
internal suspend fun collectSearchNativeAds(
    provider: AdProvider,
    resultCounts: Flow<Int>,
    onSlotsChanged: (Map<Int, NativeAdLoadState>) -> Unit,
    adLifetimeMillis: Long = 60 * 60 * 1_000L,
) {
    require(adLifetimeMillis > 0)
    coroutineScope {
        val requests = mutableMapOf<Int, Job>()
        val states = mutableMapOf<Int, NativeAdLoadState>()
        val loadingPermits = Semaphore(2)

        fun publishSlots() {
            onSlotsChanged(states.toMap())
        }

        try {
            resultCounts.distinctUntilChanged().collect { resultCount ->
                val desiredCount = SearchNativeAdPolicy.indexes(resultCount).size
                val removedRequests = requests.keys.filter { it >= desiredCount }.mapNotNull { slot ->
                    states.remove(slot)
                    requests.remove(slot)
                }
                // Publish all placeholders together before even the first request can complete.
                repeat(desiredCount) { slot -> states.putIfAbsent(slot, NativeAdLoadState.Loading) }
                publishSlots()
                removedRequests.forEach { it.cancel() }

                repeat(desiredCount) { slot ->
                    if (slot in requests) return@repeat

                    // Install the job before it can publish, even with Main.immediate dispatch.
                    val request = launch(start = CoroutineStart.LAZY) {
                        val owner = currentCoroutineContext().job

                        fun setState(state: NativeAdLoadState) {
                            if (requests[slot] === owner) {
                                states[slot] = state
                                publishSlots()
                            }
                        }

                        while (isActive) {
                            setState(NativeAdLoadState.Loading)
                            val ad = try {
                                loadingPermits.withPermit { provider.loadNativeAd() }
                            } catch (error: CancellationException) {
                                throw error
                            } catch (_: Exception) {
                                null
                            }
                            if (ad == null) {
                                setState(NativeAdLoadState.Failed)
                                delay(adLifetimeMillis)
                                continue
                            }

                            try {
                                setState(NativeAdLoadState.Loaded(ad))
                                delay(adLifetimeMillis)
                            } finally {
                                // Remove the old resource from the UI before releasing its views.
                                setState(NativeAdLoadState.Loading)
                                ad.destroy()
                            }
                        }
                    }
                    requests[slot] = request
                    request.start()
                }
            }
            awaitCancellation()
        } finally {
            val ownedRequests = requests.values.toList()
            requests.clear()
            states.clear()
            publishSlots()
            ownedRequests.forEach { it.cancel() }
        }
    }
}
