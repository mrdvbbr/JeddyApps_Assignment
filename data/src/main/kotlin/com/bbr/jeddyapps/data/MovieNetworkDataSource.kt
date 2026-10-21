package com.bbr.jeddyapps.data

import com.bbr.jeddyapps.core.network.MovieApi
import com.bbr.jeddyapps.core.network.MoviePageDto
import com.bbr.jeddyapps.core.network.OmdbApiKeyConfigured
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException
import javax.inject.Inject

interface MovieNetworkDataSource {
    suspend fun searchMovies(query: String, page: Int): MoviePageDto
}

class RetrofitMovieNetworkDataSource @Inject constructor(
    private val api: MovieApi,
    @param:OmdbApiKeyConfigured
    private val apiKeyConfigured: Boolean,
) : MovieNetworkDataSource {
    override suspend fun searchMovies(query: String, page: Int): MoviePageDto {
        checkConfiguration()
        return api.searchMovies(query, page)
    }

    private fun checkConfiguration() {
        if (!apiKeyConfigured) {
            throw MovieRepositoryException(
                "Add your OMDb API key to local.properties as OMDB_API_KEY, then rebuild the app.",
            )
        }
    }
}
