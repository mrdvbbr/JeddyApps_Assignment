package com.bbr.jeddyapps.domain.repository

import androidx.paging.PagingData
import com.bbr.jeddyapps.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun searchMovies(query: String): Flow<PagingData<Movie>>
}

class MovieRepositoryException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
