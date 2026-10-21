package com.bbr.jeddyapps.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bbr.jeddyapps.core.network.MoviePageDto
import com.bbr.jeddyapps.domain.model.MoviePage
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.data.paging.MovieSearchPagingSource
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
            throw MovieRepositoryException("Enter a movie title to search.")
        }
        return fetch(page) { networkDataSource.searchMovies(trimmedQuery, page) }
    }

    private fun checkPage(page: Int) {
        if (page !in 1..100) throw MovieRepositoryException("The movie page must be between 1 and 100.")
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
                throw MovieRepositoryException(response.error.messageForUser())
            }
            else -> throw MovieRepositoryException("OMDb returned an unreadable response. Please try again.")
        }
    } catch (error: IOException) {
        throw MovieRepositoryException("Unable to connect to OMDb. Check your connection and try again.", error)
    } catch (error: HttpException) {
        throw MovieRepositoryException(error.messageForUser(), error)
    } catch (error: JsonParseException) {
        throw MovieRepositoryException("OMDb returned an unreadable response. Please try again.", error)
    }
}

private fun HttpException.messageForUser(): String = when (code()) {
    401, 403 -> "OMDb rejected the API key. Check your OMDB_API_KEY and rebuild the app."
    429 -> "OMDb request limit reached. Please try again later."
    in 500..599 -> "OMDb is temporarily unavailable. Please try again later."
    else -> "Unable to load movies from OMDb (HTTP ${code()}). Please try again."
}

private fun String?.messageForUser(): String = when {
    this.orEmpty().contains("api key", ignoreCase = true) ->
        "OMDb rejected the API key. Check your OMDB_API_KEY and rebuild the app."
    this.orEmpty().contains("limit", ignoreCase = true) ->
        "OMDb request limit reached. Please try again later."
    this.isNullOrBlank() -> "OMDb could not complete the search. Please try again."
    else -> "OMDb could not complete the search: ${trim()} Please refine your search and try again."
}
