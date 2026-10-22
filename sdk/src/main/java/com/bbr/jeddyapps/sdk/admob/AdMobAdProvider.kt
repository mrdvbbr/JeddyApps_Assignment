package com.bbr.jeddyapps.sdk.admob

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.bbr.jeddyapps.sdk.AdProvider
import com.bbr.jeddyapps.sdk.NativeAd
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd as GoogleNativeAd

/** AdMob's implementation. Keep one instance for the lifetime of the application. */
class AdMobAdProvider(
    context: Context,
    private val configuration: AdMobConfiguration,
) : AdProvider {
    private val applicationContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val initialization = scope.async(start = CoroutineStart.LAZY) {
        val adaptersReady = CompletableDeferred<Unit>()
        val configBuilder = InitializationConfig.Builder(configuration.appId)
        if (configuration.disableNativeValidator) {
            configBuilder.setNativeValidatorDisabled()
        }
        MobileAds.initialize(
            applicationContext,
            configBuilder.build(),
        ) { adaptersReady.complete(Unit) }
        adaptersReady.await()
    }

    private var interstitial: InterstitialAd? = null
    private var interstitialLoadedAt = 0L
    private var isInterstitialLoading = false
    private var isInterstitialShowing = false
    private var expirationJob: Job? = null

    override suspend fun initialize() {
        initialization.await()
    }

    override fun preloadInterstitial() {
        scope.launch {
            try {
                initialize()
                withContext(Dispatchers.Main.immediate) { loadInterstitialIfNeeded() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.w(TAG, "Unable to prepare an interstitial", error)
            }
        }
    }

    private fun loadInterstitialIfNeeded() {
        clearExpiredInterstitial()
        if (interstitial != null || isInterstitialLoading || isInterstitialShowing) return

        isInterstitialLoading = true
        try {
            InterstitialAd.load(
                AdRequest.Builder(configuration.interstitialAdUnitId).build(),
                object : AdLoadCallback<InterstitialAd> {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        onMainThread {
                            isInterstitialLoading = false
                            interstitial?.destroy()
                            interstitial = ad
                            interstitialLoadedAt = SystemClock.elapsedRealtime()
                            expirationJob?.cancel()
                            expirationJob = scope.launch(Dispatchers.Main.immediate) {
                                delay(AD_LIFETIME_MILLIS)
                                clearExpiredInterstitial()
                            }
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        onMainThread { isInterstitialLoading = false }
                        Log.d(TAG, "Interstitial unavailable: ${adError.message}")
                    }
                },
            )
        } catch (error: Exception) {
            isInterstitialLoading = false
            throw error
        }
    }

    override fun showInterstitial(activity: Activity): Boolean {
        if (Looper.myLooper() != Looper.getMainLooper()) return false
        if (activity.isFinishing || activity.isDestroyed || isInterstitialShowing) return false

        clearExpiredInterstitial()
        val ad = interstitial ?: return false
        // A fullscreen ad is single use. Remove it before show() can deliver any callbacks.
        interstitial = null
        expirationJob?.cancel()
        expirationJob = null
        isInterstitialShowing = true
        ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdDismissedFullScreenContent() = finishAd()

            override fun onAdFailedToShowFullScreenContent(
                fullScreenContentError: FullScreenContentError,
            ) {
                Log.d(TAG, "Interstitial could not show: ${fullScreenContentError.message}")
                finishAd()
            }

            private fun finishAd() {
                onMainThread {
                    isInterstitialShowing = false
                    ad.adEventCallback = null
                    ad.destroy()
                }
            }
        }
        return try {
            ad.show(activity)
            true
        } catch (error: Exception) {
            isInterstitialShowing = false
            ad.adEventCallback = null
            ad.destroy()
            Log.w(TAG, "Unable to show an interstitial", error)
            false
        }
    }

    private fun clearExpiredInterstitial() {
        if (interstitial != null &&
            SystemClock.elapsedRealtime() - interstitialLoadedAt >= AD_LIFETIME_MILLIS
        ) {
            interstitial?.destroy()
            interstitial = null
        }
    }

    override suspend fun loadNativeAd(): NativeAd? {
        try {
            initialize()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.w(TAG, "Unable to initialize native ads", error)
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            val completed = AtomicBoolean(false)
            val callback = object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: GoogleNativeAd) {
                    val ad = AdMobNativeAd(nativeAd)
                    if (completed.compareAndSet(false, true)) {
                        // Covers cancellation before the callback and before the caller resumes.
                        continuation.resume(ad) { _, unusedAd, _ -> unusedAd?.destroy() }
                    } else {
                        ad.destroy()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Native ad unavailable: ${adError.message}")
                    if (completed.compareAndSet(false, true)) continuation.resume(null)
                }
            }
            try {
                NativeAdLoader.load(
                    NativeAdRequest.Builder(
                        configuration.nativeAdUnitId,
                        listOf(GoogleNativeAd.NativeAdType.NATIVE),
                    ).build(),
                    callback,
                )
            } catch (error: Exception) {
                Log.w(TAG, "Unable to request a native ad", error)
                if (completed.compareAndSet(false, true)) continuation.resume(null)
            }
        }
    }

    private companion object {
        const val TAG = "AdMobAdProvider"
        const val AD_LIFETIME_MILLIS = 60 * 60 * 1_000L
    }
}
