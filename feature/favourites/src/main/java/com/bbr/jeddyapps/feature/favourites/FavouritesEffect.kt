package com.bbr.jeddyapps.feature.favourites

import com.bbr.jeddyapps.domain.error.MovieError

sealed interface FavouritesEffect {
    data class ShowError(val error: MovieError) : FavouritesEffect
}
