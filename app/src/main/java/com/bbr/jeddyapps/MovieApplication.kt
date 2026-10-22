package com.bbr.jeddyapps

import android.app.Application
import com.bbr.jeddyapps.sdk.AdSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MovieApplication : Application() {
    @Inject lateinit var adSdk: AdSdk

    override fun onCreate() {
        super.onCreate()
        adSdk.start()
    }
}
