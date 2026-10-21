package com.bbr.jeddyapps.data.di

import com.bbr.jeddyapps.data.MovieNetworkDataSource
import com.bbr.jeddyapps.data.RetrofitMovieNetworkDataSource
import com.bbr.jeddyapps.data.OmdbMovieRepository
import com.bbr.jeddyapps.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MovieDataModule {
    @Binds
    @Singleton
    abstract fun bindMovieRepository(repository: OmdbMovieRepository): MovieRepository

    @Binds
    @Singleton
    abstract fun bindMovieNetworkDataSource(
        dataSource: RetrofitMovieNetworkDataSource,
    ): MovieNetworkDataSource
}
