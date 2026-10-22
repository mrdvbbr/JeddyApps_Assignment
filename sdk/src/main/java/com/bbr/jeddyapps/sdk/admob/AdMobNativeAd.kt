package com.bbr.jeddyapps.sdk.admob

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bbr.jeddyapps.sdk.NativeAd
import java.util.concurrent.atomic.AtomicBoolean
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd as GoogleNativeAd

internal class AdMobNativeAd(private val ad: GoogleNativeAd) : NativeAd {
    private val destroyed = AtomicBoolean(false)
    // View creation, registration and removal are all confined to the main thread.
    private val views = mutableSetOf<AdMobNativeAdHost>()

    @Composable
    override fun Content(modifier: Modifier) {
        if (destroyed.get()) return

        AdMobNativeAdContent(
            ad = ad,
            modifier = modifier,
            onViewCreated = { views.add(it) },
            onViewReleased = ::releaseView,
        )
    }

    private fun releaseView(view: AdMobNativeAdHost) {
        onMainThread {
            if (views.remove(view)) view.dispose()
        }
    }

    override fun destroy() {
        if (!destroyed.compareAndSet(false, true)) return
        onMainThread {
            views.forEach(AdMobNativeAdHost::dispose)
            views.clear()
            ad.destroy()
        }
    }
}
