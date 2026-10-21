package com.bbr.jeddyapps.navigation

import kotlinx.serialization.Serializable

@Serializable
internal sealed interface AppDestination {
    @Serializable
    data object Movies : AppDestination

    @Serializable
    data object Favourites : AppDestination
}
