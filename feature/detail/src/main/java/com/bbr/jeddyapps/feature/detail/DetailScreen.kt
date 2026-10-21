package com.bbr.jeddyapps.feature.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bbr.jeddyapps.core.ui.components.ErrorContent
import com.bbr.jeddyapps.core.ui.components.LoadingContent
import com.bbr.jeddyapps.core.ui.error.messageResource
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.domain.model.MovieDetail
import com.bbr.jeddyapps.feature.detail.components.DetailContent

@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit = {},
) {
    val viewModel = hiltViewModel<DetailViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel, lifecycleOwner, context, snackbarHostState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.sideEffects.collect { effect ->
                when (effect) {
                    DetailEffect.NavigateBack -> onNavigateBack()
                    is DetailEffect.ShowError -> snackbarHostState.showSnackbar(
                        context.getString(effect.error.messageResource()),
                    )
                }
            }
        }
    }

    ScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.movie?.title ?: stringResource(R.string.movie_detail_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    state.movie?.let { movie ->
                        IconButton(onClick = { onIntent(DetailIntent.ToggleLike) }) {
                            Icon(
                                imageVector = if (movie.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (movie.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> LoadingContent()
                state.error != null -> ErrorContent(
                    message = stringResource(state.error.messageResource()),
                    onRetry = { onIntent(DetailIntent.Retry) },
                )
                state.movie != null -> DetailContent(
                    movie = state.movie,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Preview(name = "Detail loaded", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DetailScreenPreview() {
    MovieTheme(dynamicColor = false) {
        ScreenContent(
            state = DetailState(
                isLoading = false,
                movie = MovieDetail(
                    id = "tt0372784",
                    title = "Batman Begins",
                    year = "2005",
                    rated = "PG-13",
                    released = "15 Jun 2005",
                    runtime = "140 min",
                    genre = "Action, Crime, Drama",
                    director = "Christopher Nolan",
                    writer = "Bob Kane, David S. Goyer, Christopher Nolan",
                    actors = "Christian Bale, Michael Caine, Ken Watanabe",
                    plot = "After training with his mentor, Batman begins his fight to free crime-ridden Gotham City from corruption.",
                    language = "English, Mandarin",
                    country = "United States, United Kingdom",
                    awards = "Nominated for 1 Oscar. 14 wins & 79 nominations total",
                    posterUrl = null,
                    imdbRating = "8.2",
                    imdbVotes = "1,600,000",
                    isLiked = true,
                ),
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
