package com.bbr.jeddyapps.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.bbr.jeddyapps.core.ui.R
import com.bbr.jeddyapps.core.ui.theme.MovieTheme

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    val label = stringResource(R.string.loading)
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = label })
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 400)
@Composable
private fun LoadingContentPreview() {
    MovieTheme(dynamicColor = false) { LoadingContent() }
}
