package com.bbr.jeddyapps.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bbr.jeddyapps.R
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.navigation.AppDestination

@Composable
internal fun AppNavigationBar(
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = selectedDestination == AppDestination.Movies,
            onClick = { onDestinationSelected(AppDestination.Movies) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_movies)) },
        )
        NavigationBarItem(
            selected = selectedDestination == AppDestination.Favourites,
            onClick = { onDestinationSelected(AppDestination.Favourites) },
            icon = {
                Icon(
                    imageVector = if (selectedDestination == AppDestination.Favourites) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = null,
                )
            },
            label = { Text(stringResource(R.string.tab_favourites)) },
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun AppNavigationBarMoviesPreview() {
    MovieTheme(dynamicColor = false) {
        AppNavigationBar(selectedDestination = AppDestination.Movies, onDestinationSelected = {})
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun AppNavigationBarFavouritesPreview() {
    MovieTheme(dynamicColor = false) {
        AppNavigationBar(selectedDestination = AppDestination.Favourites, onDestinationSelected = {})
    }
}
