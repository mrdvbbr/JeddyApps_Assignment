# SDK

Android library module `:SDK`, stored in `sdk/`. Supports interstitial and native ads through the Google Mobile Ads Next-Gen SDK. Google ad types remain inside `admob/`; no Google ad classes appear in the app, features, or domain.

## Integration

`AdsModule` provides a singleton `AdProvider` and `AdSdk`. `MovieApplication` starts the SDK and `MainActivity` attaches itself as the ad host. Home's Hilt ViewModel receives the provider and owns a `SearchNativeAdsSession` in `viewModelScope`. `rememberSearchNativeAds` observes that session with lifecycle-aware collection; `NativeAdSlotContent` renders the returned loading or loaded slots. Leaving composition does not cancel the session. `core/ui` only knows about generic inline grid cells and their stable keys.

For another provider, implement `AdProvider` and its `NativeAd` handle, then update the Hilt provider binding. The return and search placement rules stay in this module. A future provider router or fallback chain can implement the same interface without changing screens.

## Behavior

- Native: when at least three results exist, reserve slots at final, zero-based indexes 0, 3, 6, …, with up to two movies after each ad and no trailing ad. Each slot has a stable identity throughout loading and completion. Show a pulsing skeleton, reveal each newly loaded ad once over 250 ms, and animate its initial size change. Returning rows reuse the ad's reveal status and its measured height when width, density, and font scale match. Paging appends add requests for new placements without reloading existing ads. Limit simultaneous loads to two. Remove failed slots while preserving remaining slot identities; the grid animates placement changes without entry/exit fades. Refresh cached ads after one hour. Keep ads through screen navigation and transient paging restoration; destroy surplus ads when results settle to a smaller count and all ads when the owning ViewModel clears, including ads delivered after cancellation.
- Interstitial: initialize in the background and preload ahead of time. Initial launch is visit 1, the first background/foreground return is visit 2, and only visit 3 is eligible. Persist the saturated visit count in SDK preferences. Process lifecycle events ignore activity recreation and internal navigation. Show only on a resumed app activity. Consume the opportunity before showing, so dismissing an ad cannot trigger another. If loading failed or the ad expired, skip it; never show it unexpectedly after a delayed load.
- Native cards use Material 3 composables for layout, text, and the call-to-action. A small View bridge registers each Compose asset with AdMob's required `NativeAdView`; media playback uses `MediaView`. The SDK handles clicks, impressions, and AdChoices. Theme locals propagate into each nested composition, and removing a card disposes its compositions before its native container.

Google test IDs are the default; debug builds always force them. To configure release IDs, set `ADMOB_APP_ID`, `ADMOB_INTERSTITIAL_AD_UNIT_ID`, and `ADMOB_NATIVE_AD_UNIT_ID` in `local.properties` or the environment and rebuild. The Next-Gen SDK receives the app ID during initialization.

## Verification

```shell
./gradlew :SDK:testDebugUnitTest :core:ui:testDebugUnitTest :app:assembleDebug
```

On a device with network access:

1. Clear app data for a fresh return count, launch, and search for three or more movies. Confirm ads at indexes 0, 3, 6, …, identifiable by their Ad label. Scroll to load more pages and verify the pattern continues. Verify fewer than three results contain no ads and Favourites contains no ads.
2. Rotate and navigate between tabs/details: no interstitial should appear.
3. Press Home, wait at least one second for process background detection, and reopen: no interstitial on this first return.
4. Press Home again, wait at least one second, and reopen: the preloaded test interstitial appears on the second return. Dismiss it and repeat: no further interstitials.
5. Repeat with process termination between visits to verify persisted counting. To repeat the entire sequence, clear app data.
6. With network unavailable, movie loading and retry remain usable; no ad request blocks navigation or causes a late interstitial.

Implementation references: [initialization](https://developers.google.com/admob/android/next-gen/quick-start), [interstitials](https://developers.google.com/admob/android/next-gen/interstitial), [native rendering](https://developers.google.com/admob/android/next-gen/native/advanced).
