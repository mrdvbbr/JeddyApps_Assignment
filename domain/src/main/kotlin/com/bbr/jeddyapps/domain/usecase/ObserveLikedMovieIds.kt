package com.bbr.jeddyapps.domain.usecase

import com.bbr.jeddyapps.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class ObserveLikedMovieIds(private val repository: MovieRepository) {
    operator fun invoke(): Flow<Set<String>> = repository.getLikedMovieIds()
    operator fun invoke(ids: List<String>): Flow<Set<String>> = repository.observeLikedMovieIds(ids)
}
