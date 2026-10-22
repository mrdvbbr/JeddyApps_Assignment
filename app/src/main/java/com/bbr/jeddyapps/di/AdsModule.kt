package com.bbr.jeddyapps.di

import android.app.Application
import android.content.Context
import com.bbr.jeddyapps.sdk.AdProvider
import com.bbr.jeddyapps.sdk.AdSdk
import com.bbr.jeddyapps.sdk.admob.AdMobAdProvider
import com.bbr.jeddyapps.sdk.admob.AdMobConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdsModule {
    @Provides
    @Singleton
    fun provideAdProvider(@ApplicationContext context: Context): AdProvider =
        AdMobAdProvider(context, AdMobConfiguration())

    @Provides
    @Singleton
    fun provideAdSdk(application: Application, provider: AdProvider): AdSdk =
        AdSdk(application, provider)
}
