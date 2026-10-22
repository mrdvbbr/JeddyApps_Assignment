package com.bbr.jeddyapps.domain.repository

import androidx.paging.PagingData
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.model.MovieDetail
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun searchMovies(query: String): Flow<PagingData<Movie>>
    suspend fun getMovieDetail(id: String): MovieDetail
    suspend fun addLikedMovie(movie: Movie)
    suspend fun removeLikedMovie(movieId: String)
    suspend fun isMovieLiked(id: String): Boolean
    fun observeMovieLiked(id: String): Flow<Boolean>
    fun getLikedMovies(): Flow<PagingData<Movie>>
    fun getLikedMovieIds(): Flow<Set<String>>
}

class MovieRepositoryException(
    val error: MovieError,
    cause: Throwable? = null,
) : Exception(error.name, cause)
