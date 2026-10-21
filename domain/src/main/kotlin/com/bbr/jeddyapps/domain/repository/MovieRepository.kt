package com.bbr.jeddyapps.domain.repository

import androidx.paging.PagingData
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun searchMovies(query: String): Flow<PagingData<Movie>>
}

class MovieRepositoryException(
    val error: MovieError,
    cause: Throwable? = null,
) : Exception(error.name, cause)
