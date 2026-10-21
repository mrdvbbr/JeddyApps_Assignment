package com.bbr.jeddyapps.domain.usecase

import com.bbr.jeddyapps.domain.model.MovieDetail
import com.bbr.jeddyapps.domain.repository.MovieRepository

class GetMovieDetail(private val repository: MovieRepository) {
    suspend operator fun invoke(id: String): MovieDetail = repository.getMovieDetail(id)
}
