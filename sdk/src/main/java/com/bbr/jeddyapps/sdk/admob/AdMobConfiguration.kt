package com.bbr.jeddyapps.sdk.admob

import com.bbr.jeddyapps.sdk.BuildConfig

data class AdMobConfiguration(
    val appId: String = BuildConfig.ADMOB_APP_ID,
    val interstitialAdUnitId: String = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID,
    val nativeAdUnitId: String = BuildConfig.ADMOB_NATIVE_AD_UNIT_ID,
    val disableNativeValidator: Boolean = true,
)
