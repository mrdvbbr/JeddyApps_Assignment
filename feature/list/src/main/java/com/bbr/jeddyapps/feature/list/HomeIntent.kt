package com.bbr.jeddyapps.feature.list

import com.bbr.jeddyapps.domain.model.Movie

sealed interface HomeIntent {
    data class QueryChanged(val query: String) : HomeIntent
    data object ClearSearch : HomeIntent
    data class ToggleLike(val movie: Movie) : HomeIntent
}
