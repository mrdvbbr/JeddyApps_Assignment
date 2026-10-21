package com.bbr.jeddyapps.domain.usecase

import androidx.paging.PagingData
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class SearchMovies(private val repository: MovieRepository) {
    operator fun invoke(query: String): Flow<PagingData<Movie>> = repository.searchMovies(query)
}
