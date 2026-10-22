package com.bbr.jeddyapps.sdk.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.bbr.jeddyapps.sdk.NativeAd

/** Displays a caller-owned native ad. The caller remains responsible for destroying it. */
@Composable
fun NativeAdContent(
    ad: NativeAd,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) return

    key(ad) {
        ad.Content(modifier)
    }
}
