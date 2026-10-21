package com.bbr.jeddyapps.di

import com.bbr.jeddyapps.BuildConfig
import com.bbr.jeddyapps.core.network.MovieApi
import com.bbr.jeddyapps.core.network.MovieApiFactory
import com.bbr.jeddyapps.core.network.OmdbApiKeyConfigured
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideMovieApi(): MovieApi = MovieApiFactory.create(BuildConfig.OMDB_API_KEY)

    @Provides
    @OmdbApiKeyConfigured
    fun provideApiKeyConfigured(): Boolean = BuildConfig.OMDB_API_KEY.isNotBlank()
}
