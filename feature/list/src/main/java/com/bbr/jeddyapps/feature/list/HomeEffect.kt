package com.bbr.jeddyapps.feature.list

import com.bbr.jeddyapps.domain.error.MovieError

sealed interface HomeEffect {
    data object HideKeyboard : HomeEffect
    data class ShowError(val error: MovieError) : HomeEffect
}
