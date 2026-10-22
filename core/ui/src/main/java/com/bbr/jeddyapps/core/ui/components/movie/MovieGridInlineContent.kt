package com.bbr.jeddyapps.core.ui.components.movie

import androidx.compose.runtime.Composable

/**
 * A single inline grid cell. [key] must be unique across inline cells and remain stable for the
 * lifetime of the content, including when its presentation changes from loading to ready.
 */
data class MovieGridInlineContent(
    val key: Long,
    val content: @Composable () -> Unit,
)
