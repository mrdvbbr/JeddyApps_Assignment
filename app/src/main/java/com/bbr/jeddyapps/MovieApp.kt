package com.bbr.jeddyapps

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bbr.jeddyapps.components.AppNavigationBar
import com.bbr.jeddyapps.feature.favourites.FavouritesScreen
import com.bbr.jeddyapps.feature.list.HomeScreen
import com.bbr.jeddyapps.navigation.AppDestination

@Composable
fun MovieApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = if (backStackEntry?.destination?.hasRoute<AppDestination.Favourites>() == true) {
        AppDestination.Favourites
    } else {
        AppDestination.Movies
    }

    Scaffold(
        bottomBar = {
            AppNavigationBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = { destination ->
                    navController.navigate(destination) {
                        popUpTo<AppDestination.Movies> { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Movies,
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            composable<AppDestination.Movies> { HomeScreen() }
            composable<AppDestination.Favourites> { FavouritesScreen() }
        }
    }
}
