package com.bbr.jeddyapps.domain.usecase

import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.repository.MovieRepository

class ToggleMovieLike(private val repository: MovieRepository) {
    suspend operator fun invoke(movie: Movie) {
        if (movie.isLiked) {
            repository.removeLikedMovie(movie.id)
        } else {
            repository.addLikedMovie(movie)
        }
    }
}
