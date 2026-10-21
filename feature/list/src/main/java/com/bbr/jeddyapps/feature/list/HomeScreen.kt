package com.bbr.jeddyapps.feature.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.core.ui.error.messageResource
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.feature.list.components.MovieResultsContent
import com.bbr.jeddyapps.feature.list.components.MovieSearchBar
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit = {},
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingItems = viewModel.movies.collectAsLazyPagingItems()
    val keyboardController = LocalSoftwareKeyboardController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel, lifecycleOwner, keyboardController, context, snackbarHostState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.sideEffects.collect { effect ->
                when (effect) {
                    HomeEffect.HideKeyboard -> keyboardController?.hide()
                    is HomeEffect.ShowError -> snackbarHostState.showSnackbar(
                        context.getString(effect.error.messageResource()),
                    )
                }
            }
        }
    }

    LaunchedEffect(pagingItems.itemSnapshotList.size) {
        if (pagingItems.itemSnapshotList.isNotEmpty()) {
            viewModel.onIntent(HomeIntent.DisplayedMoviesChanged())
        }
    }

    ScreenContent(
        state = state,
        movies = pagingItems,
        onIntent = viewModel::onIntent,
        onMovieClick = onMovieClick,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun ScreenContent(
    state: HomeState,
    movies: LazyPagingItems<Movie>,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MovieSearchBar(
                query = state.query,
                onQueryChange = { onIntent(HomeIntent.QueryChanged(it)) },
                onClear = { onIntent(HomeIntent.ClearSearch) },
            )
            MovieResultsContent(
                movies = movies,
                likedIds = state.likedMovieIds,
                onMovieClick = onMovieClick,
                onToggleLike = { onIntent(HomeIntent.ToggleLike(it)) },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Home – initial", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    MovieTheme(dynamicColor = false) {
        val movies = remember {
            MutableStateFlow(
                PagingData.from(
                    listOf(
                        Movie("tt0096895", "Batman", "1989", null),
                        Movie("tt0372784", "Batman Begins", "2005", null),
                    ),
                ),
            )
        }
        ScreenContent(state = HomeState(), movies = movies.collectAsLazyPagingItems(), onIntent = {})
    }
}

@Preview(name = "Home – results", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenResultsPreview() {
    MovieTheme(dynamicColor = false) {
        val movies = remember {
            MutableStateFlow(
                PagingData.from(
                    listOf(
                        Movie("tt1375666", "Inception", "2010", null),
                        Movie("tt1790736", "Inception: The Cobol Job", "2010", null),
                    ),
                ),
            )
        }
        ScreenContent(
            state = HomeState(
                query = "Inception",
            ),
            movies = movies.collectAsLazyPagingItems(),
            onIntent = {},
        )
    }
}
