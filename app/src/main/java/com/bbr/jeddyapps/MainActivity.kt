package com.bbr.jeddyapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.sdk.AdSdk
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var adSdk: AdSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adSdk.attach(this)
        enableEdgeToEdge()
        setContent {
            MovieTheme {
                MovieApp()
            }
        }
    }
}
