package com.bbr.jeddyapps.di

import com.bbr.jeddyapps.domain.repository.MovieRepository
import com.bbr.jeddyapps.domain.usecase.GetLikedMovies
import com.bbr.jeddyapps.domain.usecase.GetMovieDetail
import com.bbr.jeddyapps.domain.usecase.IsMovieLiked
import com.bbr.jeddyapps.domain.usecase.ObserveLikedMovieIds
import com.bbr.jeddyapps.domain.usecase.SearchMovies
import com.bbr.jeddyapps.domain.usecase.ToggleMovieLike
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun provideSearchMovies(repository: MovieRepository): SearchMovies = SearchMovies(repository)

    @Provides
    fun provideToggleMovieLike(repository: MovieRepository): ToggleMovieLike = ToggleMovieLike(repository)

    @Provides
    fun provideIsMovieLiked(repository: MovieRepository): IsMovieLiked = IsMovieLiked(repository)

    @Provides
    fun provideGetLikedMovies(repository: MovieRepository): GetLikedMovies = GetLikedMovies(repository)

    @Provides
    fun provideGetMovieDetail(repository: MovieRepository): GetMovieDetail = GetMovieDetail(repository)

    @Provides
    fun provideObserveLikedMovieIds(repository: MovieRepository): ObserveLikedMovieIds =
        ObserveLikedMovieIds(repository)
}
