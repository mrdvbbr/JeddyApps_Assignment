package com.bbr.jeddyapps.di

import com.bbr.jeddyapps.domain.repository.MovieRepository
import com.bbr.jeddyapps.domain.usecase.SearchMovies
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun provideSearchMovies(repository: MovieRepository): SearchMovies = SearchMovies(repository)
}
