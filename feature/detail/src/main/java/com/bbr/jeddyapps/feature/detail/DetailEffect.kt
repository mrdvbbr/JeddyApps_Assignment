package com.bbr.jeddyapps.feature.detail

import com.bbr.jeddyapps.domain.error.MovieError

sealed interface DetailEffect {
    data object NavigateBack : DetailEffect
    data class ShowError(val error: MovieError) : DetailEffect
}
