package com.bbr.jeddyapps.feature.favourites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.bbr.jeddyapps.core.ui.error.messageResource
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.feature.favourites.components.FavouritesContent
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun FavouritesScreen(
    onMovieClick: (Movie) -> Unit = {},
) {
    val viewModel = hiltViewModel<FavouritesViewModel>()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel, lifecycleOwner, context, snackbarHostState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.sideEffects.collect { effect ->
                when (effect) {
                    is FavouritesEffect.ShowError -> snackbarHostState.showSnackbar(
                        context.getString(effect.error.messageResource()),
                    )
                }
            }
        }
    }

    ScreenContent(
        movies = movies,
        onIntent = viewModel::onIntent,
        onMovieClick = onMovieClick,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun ScreenContent(
    movies: LazyPagingItems<Movie>,
    onIntent: (FavouritesIntent) -> Unit,
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        FavouritesContent(
            movies = movies,
            onMovieClick = onMovieClick,
            onToggleLike = { onIntent(FavouritesIntent.ToggleLike(it)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        )
    }
}

@Preview(name = "Favourites grid", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FavouritesScreenPreview() {
    val movies = remember {
        MutableStateFlow(
            PagingData.from(
                listOf(
                    Movie("tt0096895", "Batman", "1989", null, isLiked = true),
                    Movie("tt0372784", "Batman Begins", "2005", null, isLiked = true),
                ),
            ),
        )
    }.collectAsLazyPagingItems()
    MovieTheme(dynamicColor = false) { ScreenContent(movies = movies, onIntent = {}) }
}
