package com.bbr.jeddyapps.domain.model

data class Movie(
    val id: String,
    val title: String,
    val year: String,
    val posterUrl: String?,
    val isLiked: Boolean = false,
)

data class MoviePage(
    val movies: List<Movie>,
    val page: Int,
    val totalPages: Int,
)
