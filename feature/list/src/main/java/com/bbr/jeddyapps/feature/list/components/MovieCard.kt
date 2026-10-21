package com.bbr.jeddyapps.feature.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbr.jeddyapps.core.ui.components.MoviePoster
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.feature.list.R

@Composable
internal fun MovieCard(movie: Movie, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        MoviePoster(
            imageUrl = movie.posterUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f),
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = movie.title.ifBlank { stringResource(R.string.untitled_movie) },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = movie.year.ifBlank { stringResource(R.string.year_unavailable) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 180)
@Composable
private fun MovieCardPreview() {
    MovieTheme(dynamicColor = false) {
        MovieCard(movie = Movie("tt1375666", "Inception", "2010", null))
    }
}
