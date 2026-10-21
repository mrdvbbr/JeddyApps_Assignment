package com.bbr.jeddyapps.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.bbr.jeddyapps.core.ui.R

@Composable
fun MoviePoster(imageUrl: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    var unavailable by remember(imageUrl) { mutableStateOf(imageUrl == null) }
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (unavailable) {
            Text(
                text = stringResource(R.string.no_image),
                modifier = Modifier.padding(12.dp).semantics {
                    contentDescription?.let { this.contentDescription = it }
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            onError = { unavailable = true },
            onSuccess = { unavailable = false },
        )
    }
}
