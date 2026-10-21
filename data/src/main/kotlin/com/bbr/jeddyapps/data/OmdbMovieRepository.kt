package com.bbr.jeddyapps.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bbr.jeddyapps.core.network.MoviePageDto
import com.bbr.jeddyapps.domain.model.MoviePage
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.data.paging.MovieSearchPagingSource
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.repository.MovieRepository
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class OmdbMovieRepository @Inject constructor(
    private val networkDataSource: MovieNetworkDataSource,
) : MovieRepository {
    override fun searchMovies(query: String): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
            prefetchDistance = 2,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { MovieSearchPagingSource { page -> loadPage(query, page) } },
    ).flow

    private suspend fun loadPage(query: String, page: Int): MoviePage {
        checkPage(page)
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            throw MovieRepositoryException(MovieError.INVALID_QUERY)
        }
        return fetch(page) { networkDataSource.searchMovies(trimmedQuery, page) }
    }

    private fun checkPage(page: Int) {
        if (page !in 1..100) throw MovieRepositoryException(MovieError.INVALID_QUERY)
    }

    private suspend fun fetch(page: Int, request: suspend () -> MoviePageDto): MoviePage = try {
        val response = request()
        when {
            response.response.equals("True", ignoreCase = true) -> response.toDomain(page)
            response.response.equals("False", ignoreCase = true) &&
                response.error?.trim().equals("Movie not found!", ignoreCase = true) -> {
                MoviePage(movies = emptyList(), page = page, totalPages = 0)
            }
            response.response.equals("False", ignoreCase = true) -> {
                throw MovieRepositoryException(
                    response.error.toMovieError(),
                    IllegalStateException(response.error),
                )
            }
            else -> throw MovieRepositoryException(MovieError.INVALID_RESPONSE)
        }
    } catch (error: IOException) {
        throw MovieRepositoryException(MovieError.NETWORK, error)
    } catch (error: HttpException) {
        throw MovieRepositoryException(error.toMovieError(), error)
    } catch (error: JsonParseException) {
        throw MovieRepositoryException(MovieError.INVALID_RESPONSE, error)
    }
}

private fun HttpException.toMovieError(): MovieError = when (code()) {
    401, 403 -> MovieError.SERVICE_UNAVAILABLE
    429 -> MovieError.RATE_LIMITED
    in 500..599 -> MovieError.SERVICE_UNAVAILABLE
    else -> MovieError.UNKNOWN
}

private fun String?.toMovieError(): MovieError = when {
    this.orEmpty().contains("api key", ignoreCase = true) -> MovieError.SERVICE_UNAVAILABLE
    this.orEmpty().contains("limit", ignoreCase = true) -> MovieError.RATE_LIMITED
    this.orEmpty().contains("too many results", ignoreCase = true) -> MovieError.INVALID_QUERY
    else -> MovieError.UNKNOWN
}
