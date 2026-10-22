package com.bbr.jeddyapps.sdk

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** An ad resource that can be rendered without exposing a provider's native ad type. */
interface NativeAd {
    /** Renders the provider's ad assets without exposing its SDK types to the screen. */
    @Composable
    fun Content(modifier: Modifier = Modifier)

    /** Releases the ad and all remaining views. Implementations must be idempotent. */
    fun destroy()
}
