package com.bbr.jeddyapps.domain.usecase

import com.bbr.jeddyapps.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class IsMovieLiked(private val repository: MovieRepository) {
    operator fun invoke(id: String): Flow<Boolean> = repository.observeMovieLiked(id)
    suspend fun check(id: String): Boolean = repository.isMovieLiked(id)
}
