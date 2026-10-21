package com.bbr.jeddyapps.feature.list.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbr.jeddyapps.core.ui.theme.MovieTheme
import com.bbr.jeddyapps.feature.list.R

@Composable
internal fun MovieSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.search_movies)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search))
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Preview(name = "Empty search", showBackground = true, widthDp = 390)
@Composable
private fun MovieSearchBarPreview() {
    MovieTheme(dynamicColor = false) {
        Surface {
            MovieSearchBar(query = "", onQueryChange = {}, onClear = {})
        }
    }
}

@Preview(name = "Search query", showBackground = true, widthDp = 390)
@Composable
private fun MovieSearchBarWithQueryPreview() {
    MovieTheme(dynamicColor = false) {
        Surface {
            MovieSearchBar(query = "Inception", onQueryChange = {}, onClear = {})
        }
    }
}
