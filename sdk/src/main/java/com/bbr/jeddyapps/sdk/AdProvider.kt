package com.bbr.jeddyapps.sdk

import android.app.Activity

/** A provider boundary that keeps ad network SDK types out of application code. */
interface AdProvider {
    /** Safe to call repeatedly; ad requests must wait for initialization to finish. */
    suspend fun initialize()

    /** Prepares at most one interstitial without keeping an Activity reference. */
    fun preloadInterstitial()

    /**
     * Shows an already loaded ad on the main thread. Returns false if no ad is ready.
     * Never queues a delayed display: eligibility belongs to the SDK's placement policy.
     */
    fun showInterstitial(activity: Activity): Boolean

    /** Returns a caller-owned ad, or null when the request cannot be filled. */
    suspend fun loadNativeAd(): NativeAd?
}
