package com.bbr.jeddyapps.feature.list

sealed interface HomeIntent {
    data class QueryChanged(val query: String) : HomeIntent
    data object ClearSearch : HomeIntent
}
