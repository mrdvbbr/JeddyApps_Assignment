package com.bbr.jeddyapps.core.ui.components.movie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.core.ui.error.messageResource
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MovieGrid(
    movies: LazyPagingItems<Movie>,
    onToggleLike: (Movie) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(count = movies.itemCount, key = movies.itemKey(Movie::id)) { index ->
            movies[index]?.let { movie ->
                MovieCard(
                    movie = movie,
                    onToggleLike = { onToggleLike(movie) },
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            val error = (movies.loadState.append as? LoadState.Error)?.error
                ?: (movies.loadState.refresh as? LoadState.Error)?.error
            MoviePagingFooter(
                isLoading = movies.loadState.append is LoadState.Loading,
                errorMessage = error?.let { stringResource(it.messageResource()) },
                onRetry = movies::retry,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 500)
@Composable
private fun MovieGridPreview() {
    val movies = remember {
        MutableStateFlow(
            PagingData.from(
                listOf(
                    Movie("tt1375666", "Inception", "2010", null),
                    Movie("tt1790736", "Inception: The Cobol Job", "2010", null),
                ),
            ),
        )
    }.collectAsLazyPagingItems()
    MovieTheme(dynamicColor = false) {
        Surface {
            MovieGrid(movies = movies)
        }
    }
}
