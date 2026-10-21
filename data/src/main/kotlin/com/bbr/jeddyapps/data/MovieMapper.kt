package com.bbr.jeddyapps.data

import com.bbr.jeddyapps.core.local.MovieEntity
import com.bbr.jeddyapps.core.network.MovieDetailDto
import com.bbr.jeddyapps.core.network.MovieDto
import com.bbr.jeddyapps.core.network.MoviePageDto
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.model.MovieDetail
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
        title = title.orEmpty(),
        year = year.orEmpty(),
        posterUrl = posterUrl?.trim()?.takeIf { it.isNotEmpty() && !it.equals("N/A", ignoreCase = true) },
    )
}

internal fun Movie.toEntity(): MovieEntity = MovieEntity(
    id = id,
    title = title,
    year = year,
    posterUrl = posterUrl,
)

internal fun MovieEntity.toDomain(): Movie = Movie(
    id = id,
    title = title,
    year = year,
    posterUrl = posterUrl,
    isLiked = true,
)

internal fun MovieDetailDto.toDomain(isLiked: Boolean): MovieDetail {
    val clean = { value: String? ->
        value?.trim()?.takeIf { it.isNotEmpty() && !it.equals("N/A", ignoreCase = true) }
    }
    return MovieDetail(
        id = id?.trim().orEmpty(),
        title = title?.trim().orEmpty(),
        year = year?.trim().orEmpty(),
        rated = clean(rated),
        released = clean(released),
        runtime = clean(runtime),
        genre = clean(genre),
        director = clean(director),
        writer = clean(writer),
        actors = clean(actors),
        plot = clean(plot),
        language = clean(language),
        country = clean(country),
        awards = clean(awards),
        posterUrl = clean(posterUrl),
        imdbRating = clean(imdbRating),
        imdbVotes = clean(imdbVotes),
        isLiked = isLiked,
    )
}
