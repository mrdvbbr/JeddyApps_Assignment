package com.bbr.jeddyapps.feature.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.bbr.jeddyapps.core.ui.components.EmptyContent
import com.bbr.jeddyapps.core.ui.components.ErrorContent
import com.bbr.jeddyapps.core.ui.components.LoadingContent
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException
import com.bbr.jeddyapps.feature.list.R
import com.bbr.jeddyapps.feature.list.messageResource
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
internal fun MovieResultsContent(
    movies: LazyPagingItems<Movie>,
    modifier: Modifier = Modifier,
) {
    val refresh = movies.loadState.refresh
    Box(modifier = modifier) {
        when {
            refresh is LoadState.Loading && movies.itemCount == 0 -> LoadingContent()
            refresh is LoadState.Error && movies.itemCount == 0 -> ErrorContent(
                message = stringResource(refresh.error.messageResource()),
                onRetry = movies::retry,
            )
            movies.itemCount == 0 -> EmptyContent(message = stringResource(R.string.no_movies_found))
            else -> MovieGrid(movies = movies, modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(name = "Empty", showBackground = true, widthDp = 390, heightDp = 400)
@Composable
private fun MovieResultsContentPreview() {
    val movies = remember { previewMovies(LoadState.NotLoading(true)) }.collectAsLazyPagingItems()
    MovieTheme(dynamicColor = false) {
        Surface {
            MovieResultsContent(movies = movies)
        }
    }
}

@Preview(name = "Loading", showBackground = true, widthDp = 390, heightDp = 400)
@Composable
private fun MovieResultsContentLoadingPreview() {
    val movies = remember { previewMovies(LoadState.Loading) }.collectAsLazyPagingItems()
    MovieTheme(dynamicColor = false) {
        Surface {
            MovieResultsContent(movies = movies)
        }
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 390, heightDp = 400)
@Composable
private fun MovieResultsContentErrorPreview() {
    val movies = remember {
        previewMovies(LoadState.Error(MovieRepositoryException(MovieError.NETWORK)))
    }.collectAsLazyPagingItems()
    MovieTheme(dynamicColor = false) {
        Surface {
            MovieResultsContent(movies = movies)
        }
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
