package com.bbr.jeddyapps.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.bbr.jeddyapps.core.local.MovieDao
import com.bbr.jeddyapps.core.network.MovieDetailDto
import com.bbr.jeddyapps.core.network.MoviePageDto
import com.bbr.jeddyapps.domain.model.MoviePage
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.model.MovieDetail
import com.bbr.jeddyapps.data.paging.MovieSearchPagingSource
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.repository.MovieRepository
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class OmdbMovieRepository @Inject constructor(
    private val networkDataSource: MovieNetworkDataSource,
    private val movieDao: MovieDao,
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

    override suspend fun addLikedMovie(movie: Movie) {
        movieDao.insert(movie.toEntity())
    }

    override suspend fun removeLikedMovie(movieId: String) {
        movieDao.deleteById(movieId)
    }

    override suspend fun isMovieLiked(id: String): Boolean = movieDao.isMovieLikedSync(id)

    override fun observeMovieLiked(id: String): Flow<Boolean> = movieDao.isMovieLiked(id)

    override fun getLikedMovies(): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false),
        pagingSourceFactory = movieDao::getLikedMoviesPagingSource,
    ).flow.map { page -> page.map { it.toDomain() } }

    override fun getLikedMovieIds(): Flow<Set<String>> =
        movieDao.getAllLikedMovieIds().map { it.toSet() }

    override fun observeLikedMovieIds(ids: List<String>): Flow<Set<String>> {
        if (ids.isEmpty()) return flowOf(emptySet())
        return movieDao.observeLikedIds(ids).map { it.toSet() }
    }

    override suspend fun getMovieDetail(id: String): MovieDetail {
        val trimmedId = id.trim()
        if (trimmedId.isEmpty()) {
            throw MovieRepositoryException(MovieError.INVALID_QUERY)
        }
        val isLiked = movieDao.isMovieLikedSync(trimmedId)
        val dto = fetchDetail { networkDataSource.getMovieDetail(trimmedId) }
        return dto.toDomain(isLiked = isLiked)
    }

    private suspend fun fetchDetail(request: suspend () -> MovieDetailDto): MovieDetailDto = try {
        val response = request()
        when {
            response.response.equals("True", ignoreCase = true) -> response
            response.response.equals("False", ignoreCase = true) -> {
                throw MovieRepositoryException(
                    response.error.toMovieError(),
                    IllegalStateException(response.error),
                )
            }
            else -> throw MovieRepositoryException(MovieError.INVALID_RESPONSE)
        }
    } catch (error: MovieRepositoryException) {
        throw error
    } catch (error: IOException) {
        throw MovieRepositoryException(MovieError.NETWORK, error)
    } catch (error: HttpException) {
        throw MovieRepositoryException(error.toMovieError(), error)
    } catch (error: JsonParseException) {
        throw MovieRepositoryException(MovieError.INVALID_RESPONSE, error)
    }

    private suspend fun loadPage(query: String, page: Int): MoviePage {
        checkPage(page)
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            throw MovieRepositoryException(MovieError.INVALID_QUERY)
        }
        val moviePage = fetch(page) { networkDataSource.searchMovies(trimmedQuery, page) }
        val ids = moviePage.movies.map { it.id }
        val likedIds = if (ids.isNotEmpty()) movieDao.getLikedIds(ids).toSet() else emptySet()
        val enrichedMovies = moviePage.movies.map { movie ->
            movie.copy(isLiked = movie.id in likedIds)
        }
        return moviePage.copy(movies = enrichedMovies)
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
