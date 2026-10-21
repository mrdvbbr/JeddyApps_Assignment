package com.bbr.jeddyapps.data

import com.bbr.jeddyapps.core.network.MovieDto
import com.bbr.jeddyapps.core.network.MoviePageDto
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.model.MoviePage

internal fun MoviePageDto.toDomain(page: Int): MoviePage = MoviePage(
    movies = results.orEmpty().mapNotNull(MovieDto::toDomain).distinctBy(Movie::id),
    page = page,
    totalPages = totalPages(),
)

private fun MoviePageDto.totalPages(): Int {
    val count = totalResults?.toLongOrNull()?.coerceAtLeast(0) ?: 0L
    return (count / 10 + if (count % 10 == 0L) 0 else 1).coerceAtMost(100).toInt()
}

internal fun MovieDto.toDomain(): Movie? {
    val movieId = id?.trim()?.takeIf { it.isNotEmpty() && !it.equals("N/A", ignoreCase = true) }
        ?: return null
    return Movie(
        id = movieId,
        title = title?.takeIf(String::isNotBlank) ?: "Untitled movie",
        year = year.orEmpty(),
        posterUrl = posterUrl?.trim()?.takeIf { it.isNotEmpty() && !it.equals("N/A", ignoreCase = true) },
    )
}
