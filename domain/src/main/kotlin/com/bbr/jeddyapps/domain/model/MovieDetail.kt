package com.bbr.jeddyapps.domain.model

data class MovieDetail(
    val id: String,
    val title: String,
    val year: String,
    val rated: String?,
    val released: String?,
    val runtime: String?,
    val genre: String?,
    val director: String?,
    val writer: String?,
    val actors: String?,
    val plot: String?,
    val language: String?,
    val country: String?,
    val awards: String?,
    val posterUrl: String?,
    val imdbRating: String?,
    val imdbVotes: String?,
    val isLiked: Boolean = false,
) {
    fun toMovie(): Movie = Movie(
        id = id,
        title = title,
        year = year,
        posterUrl = posterUrl,
        isLiked = isLiked,
    )
}
