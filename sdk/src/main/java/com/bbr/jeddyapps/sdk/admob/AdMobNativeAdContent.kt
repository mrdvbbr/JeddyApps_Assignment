package com.bbr.jeddyapps.sdk.admob

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bbr.jeddyapps.sdk.R
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

@Composable
internal fun AdMobNativeAdContent(
    ad: NativeAd,
    modifier: Modifier,
    onViewCreated: (AdMobNativeAdHost) -> Unit,
    onViewReleased: (AdMobNativeAdHost) -> Unit,
) {
    val composition = rememberCompositionContext()
    AndroidView(
        factory = { context ->
            AdMobNativeAdHost(context, ad, composition).also(onViewCreated)
        },
        modifier = modifier,
        onRelease = onViewReleased,
    )
}

/** Owns the small View bridge required for AdMob asset registration and media playback. */
internal class AdMobNativeAdHost(
    context: Context,
    private val ad: NativeAd,
    composition: CompositionContext,
) : FrameLayout(context) {
    private val adView = NativeAdView(context)
    private val contentView = ComposeView(context)
    private var mediaView: MediaView? = null
    private var registered = false
    private var disposed = false

    init {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        contentView.setParentCompositionContext(composition)
        adView.addView(contentView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(adView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        contentView.setContent {
            NativeAdCard(ad, this)
        }
    }

    fun registerAsset(asset: AdMobAsset, view: View) {
        if (disposed) return
        when (asset) {
            AdMobAsset.HEADLINE -> adView.headlineView = view
            AdMobAsset.ADVERTISER -> adView.advertiserView = view
            AdMobAsset.BODY -> adView.bodyView = view
            AdMobAsset.CALL_TO_ACTION -> adView.callToActionView = view
            AdMobAsset.ICON -> adView.iconView = view
            AdMobAsset.STAR_RATING -> adView.starRatingView = view
            AdMobAsset.PRICE -> adView.priceView = view
            AdMobAsset.STORE -> adView.storeView = view
        }
    }

    fun registerMedia(view: MediaView) {
        if (!disposed) mediaView = view
    }

    fun bindWhenReady() {
        if (disposed || registered || mediaView == null || adView.headlineView == null) return
        // Layout has placed every asset's AndroidView. Register once before media starts playback.
        registered = true
        adView.registerNativeAd(ad, mediaView)
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        // This also disposes each asset's nested composition before destroying its SDK container.
        contentView.disposeComposition()
        adView.destroy()
        removeAllViews()
    }
}

internal enum class AdMobAsset {
    HEADLINE, ADVERTISER, BODY, CALL_TO_ACTION, ICON, STAR_RATING, PRICE, STORE,
}

/** Registers a Compose asset's host View so AdMob owns its clicks and impressions. */
@Composable
private fun NativeAdAsset(
    host: AdMobNativeAdHost,
    asset: AdMobAsset,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val composition = rememberCompositionContext()
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                setParentCompositionContext(composition)
                host.registerAsset(asset, this)
            }
        },
        update = { it.setContent(content) },
        onRelease = ComposeView::disposeComposition,
        modifier = modifier,
    )
}

@Composable
private fun NativeAdCard(ad: NativeAd, host: AdMobNativeAdHost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
        ) {
            AndroidView(
                factory = { context ->
                    MediaView(context).apply {
                        imageScaleType = ImageView.ScaleType.CENTER_CROP
                        host.registerMedia(this)
                    }
                },
                modifier = Modifier.matchParentSize(),
            )
            // Mirrors the like button in MovieCard: 36.dp circle in top corner with surface alpha 0.7
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
                .onGloballyPositioned { host.bindWhenReady() },
        ) {
            NativeAdAsset(host, AdMobAsset.HEADLINE) {
                Text(
                    text = ad.headline.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val actionText = ad.callToAction?.takeIf(String::isNotBlank)
                ?: ad.advertiser?.takeIf(String::isNotBlank)
                ?: ad.body?.takeIf(String::isNotBlank)
                ?: stringResource(R.string.sdk_ad_learn_more)
            val assetType = when {
                !ad.callToAction.isNullOrBlank() -> AdMobAsset.CALL_TO_ACTION
                !ad.advertiser.isNullOrBlank() -> AdMobAsset.ADVERTISER
                else -> AdMobAsset.BODY
            }
            NativeAdAsset(host, assetType) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
