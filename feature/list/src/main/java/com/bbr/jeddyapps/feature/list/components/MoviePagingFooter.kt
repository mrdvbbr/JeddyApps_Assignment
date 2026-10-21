package com.bbr.jeddyapps.feature.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.core.ui.R as CoreUiR

@Composable
internal fun MoviePagingFooter(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isLoading || errorMessage != null) {
        Column(
            modifier = modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(CoreUiR.string.retry))
                }
            }
        }
    }
}

@Preview(name = "Loading more", showBackground = true, widthDp = 390)
@Composable
private fun MoviePagingFooterLoadingPreview() {
    MovieTheme(dynamicColor = false) {
        Surface { MoviePagingFooter(isLoading = true, errorMessage = null, onRetry = {}) }
    }
}

@Preview(name = "Retry page", showBackground = true, widthDp = 390)
@Composable
private fun MoviePagingFooterErrorPreview() {
    MovieTheme(dynamicColor = false) {
        Surface {
            MoviePagingFooter(
                isLoading = false,
                errorMessage = stringResource(CoreUiR.string.error_network),
                onRetry = {},
            )
        }
    }
}
