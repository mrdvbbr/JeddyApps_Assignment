package com.bbr.jeddyapps.sdk.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bbr.jeddyapps.sdk.R

/** Reserves a card while loading, then fades in the caller-owned ad in the same lazy item. */
@Composable
fun NativeAdSlotContent(slot: NativeAdSlot, modifier: Modifier = Modifier) {
    if (slot.state is NativeAdLoadState.Failed) return
    val loaded = slot.state as? NativeAdLoadState.Loaded
    val alreadyRevealed = remember(loaded) { loaded?.presentation?.hasRevealed == true }
    val reveal = remember(loaded) { Animatable(if (alreadyRevealed) 1f else 0f) }
    var adMeasured by remember(loaded) { mutableStateOf(false) }
    val showPlaceholder by remember(reveal) { derivedStateOf { reveal.value < 1f } }

    LaunchedEffect(loaded, adMeasured) {
        if (loaded != null && adMeasured) {
            if (loaded.presentation.claimReveal()) reveal.animateTo(1f, tween(250))
            else reveal.snapTo(1f)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val cachedHeight = if (alreadyRevealed) loaded?.presentation?.measuredHeight(
            width = constraints.maxWidth,
            density = density.density,
            fontScale = density.fontScale,
        ) else null
        val minModifier = if (cachedHeight != null) {
            Modifier.heightIn(min = with(density) { cachedHeight.toDp() })
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(minModifier),
        ) {
            if (showPlaceholder) {
                NativeAdLoadingPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = 1f - reveal.value },
                )
            }
            if (loaded != null) {
                // Recreated rows reuse the resource's height and skip its completed reveal.
                NativeAdContent(
                    ad = loaded.ad,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged {
                            loaded.presentation.recordMeasurement(it, density.density, density.fontScale)
                            adMeasured = it.width > 0 && it.height > 0
                        }
                        .graphicsLayer { alpha = reveal.value },
                )
            }
        }
    }
}

@Composable
private fun NativeAdLoadingPlaceholder(modifier: Modifier = Modifier) {
    val loadingDescription = stringResource(R.string.sdk_ad_loading)
    val transition = rememberInfiniteTransition(label = "nativeAdLoading")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "nativeAdSkeletonAlpha",
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = loadingDescription },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape,
                    )
                    .size(36.dp),
            ) {
                Text(
                    text = stringResource(R.string.sdk_ad_attribution),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .graphicsLayer { alpha = pulse },
        ) {
            SkeletonBlock(
                Modifier
                    .fillMaxWidth()
                    .height(18.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            SkeletonBlock(
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(18.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBlock(
                Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp),
            )
        }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier = Modifier) {
    Box(
        modifier.background(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
            shape = MaterialTheme.shapes.small,
        ),
    )
}
