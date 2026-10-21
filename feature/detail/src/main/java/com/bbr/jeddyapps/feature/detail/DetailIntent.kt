package com.bbr.jeddyapps.feature.detail

sealed interface DetailIntent {
    data object Retry : DetailIntent
    data object ToggleLike : DetailIntent
    data object BackClicked : DetailIntent
}
