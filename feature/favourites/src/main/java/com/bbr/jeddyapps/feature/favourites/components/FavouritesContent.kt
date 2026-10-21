package com.bbr.jeddyapps.feature.favourites.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.bbr.jeddyapps.core.ui.components.ErrorContent
import com.bbr.jeddyapps.core.ui.components.LoadingContent
import com.bbr.jeddyapps.core.ui.components.movie.MovieGrid
import com.bbr.jeddyapps.core.ui.error.messageResource
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
internal fun FavouritesContent(
    movies: LazyPagingItems<Movie>,
    onToggleLike: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
) {
    val refresh = movies.loadState.refresh
    Box(modifier = modifier.fillMaxSize()) {
        when {
            refresh is LoadState.Loading && movies.itemCount == 0 -> LoadingContent()
            refresh is LoadState.Error && movies.itemCount == 0 -> ErrorContent(
                message = stringResource(refresh.error.messageResource()),
                onRetry = movies::retry,
            )
            movies.itemCount == 0 -> FavouritesEmptyContent()
            else -> MovieGrid(
                movies = movies,
                onMovieClick = onMovieClick,
                onToggleLike = onToggleLike,
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            )
        }
    }
}

@Preview(name = "Empty favourites", showBackground = true, widthDp = 390, heightDp = 500)
@Composable
private fun FavouritesContentPreview() {
    val movies = remember { previewMovies(LoadState.NotLoading(true)) }.collectAsLazyPagingItems()
    MovieTheme(dynamicColor = false) {
        Surface { FavouritesContent(movies = movies, onToggleLike = {}) }
    }
}

@Preview(name = "Loading favourites", showBackground = true, widthDp = 390, heightDp = 500)
@Composable
private fun FavouritesContentLoadingPreview() {
    val movies = remember { previewMovies(LoadState.Loading) }.collectAsLazyPagingItems()
    MovieTheme(dynamicColor = false) {
        Surface { FavouritesContent(movies = movies, onToggleLike = {}) }
    }
}

@Preview(name = "Favourites error", showBackground = true, widthDp = 390, heightDp = 500)
@Composable
private fun FavouritesContentErrorPreview() {
    val movies = remember {
        previewMovies(LoadState.Error(MovieRepositoryException(MovieError.UNKNOWN)))
    }.collectAsLazyPagingItems()
    MovieTheme(dynamicColor = false) {
        Surface { FavouritesContent(movies = movies, onToggleLike = {}) }
    }
}

private fun previewMovies(refresh: LoadState) = MutableStateFlow(
    PagingData.empty<Movie>(
        sourceLoadStates = LoadStates(
            refresh = refresh,
            prepend = LoadState.NotLoading(true),
            append = LoadState.NotLoading(true),
        ),
    ),
)
