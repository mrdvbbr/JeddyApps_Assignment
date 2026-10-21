package com.bbr.jeddyapps.di

import android.content.Context
import com.bbr.jeddyapps.core.local.MovieDao
import com.bbr.jeddyapps.core.local.MovieDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMovieDatabase(@ApplicationContext context: Context): MovieDatabase =
        MovieDatabase.create(context)

    @Provides
    fun provideMovieDao(database: MovieDatabase): MovieDao = database.movieDao()
}
