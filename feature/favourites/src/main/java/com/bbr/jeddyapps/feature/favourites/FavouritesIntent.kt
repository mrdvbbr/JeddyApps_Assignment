package com.bbr.jeddyapps.feature.favourites

import com.bbr.jeddyapps.domain.model.Movie

sealed interface FavouritesIntent {
    data class ToggleLike(val movie: Movie) : FavouritesIntent
}
