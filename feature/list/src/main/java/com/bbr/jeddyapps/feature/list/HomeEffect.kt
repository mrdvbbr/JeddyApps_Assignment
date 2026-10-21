package com.bbr.jeddyapps.feature.list

sealed interface HomeEffect {
    data object HideKeyboard : HomeEffect
}
