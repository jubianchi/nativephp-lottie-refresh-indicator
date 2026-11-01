package com.jubianchi.nativephp.lottierefreshindicator

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimatable
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.nativephp.plugins.native_ui.NativeUIRefreshIndicators
import com.nativephp.plugins.native_ui.RefreshIndicatorScope

const val LOTTIE_REFRESH_INDICATOR_TAG = "lottie-refresh-indicator"

/**
 * Registers the `lottie` pull-to-refresh indicator with mobile-ui. Declared
 * as the plugin's `android.init_function`, which core calls before the
 * first composition.
 */
@Suppress("UNUSED_PARAMETER")
fun registerLottieRefreshIndicator(context: Context) {
    NativeUIRefreshIndicators.register("lottie") { scope -> LottieRefreshIndicator(scope) }
}

/** The segments the composition's `pull` and `refresh` markers give. */
fun LottieComposition.refreshSegments(): LottieRefreshSegments =
    lottieRefreshSegments(markerProgress("pull"), markerProgress("refresh"))

private fun LottieComposition.markerProgress(name: String): ClosedFloatingPointRange<Float>? {
    val marker = getMarker(name) ?: return null
    // The parser stores the end frame as `op - 0.01`, so a marker ending at
    // `op` lands just past 1.
    val start = getProgressForFrame(marker.startFrame).coerceIn(0f, 1f)
    val end = getProgressForFrame(marker.startFrame + marker.durationFrames).coerceIn(0f, 1f)
    return start..end
}

/**
 * Plays the refresh from where [pull] left the animation, without a jump:
 * up to the refresh segment, then that segment in a forward loop. A loop
 * clipped to the segment would snap to the segment's start on its first
 * frame if it started before it, hence the separate run up to it.
 *
 * Without a refresh segment, the pull segment plays back and forth
 * instead: looping it forward would wrap to its start, which is blank for
 * a draw-in logo.
 */
suspend fun LottieAnimatable.playRefreshLoop(composition: LottieComposition, segments: LottieRefreshSegments, pull: Float) {
    val start = segments.pullProgress(pull)
    val refresh = segments.refresh
    if (refresh == null) {
        animate(
            composition,
            iterations = LottieConstants.IterateForever,
            reverseOnRepeat = true,
            clipSpec = LottieClipSpec.Progress(segments.pull.start, segments.pull.endInclusive),
            initialProgress = start,
        )
        return
    }

    if (start < refresh.start) {
        animate(
            composition,
            iteration = 1,
            iterations = 1,
            reverseOnRepeat = false,
            clipSpec = LottieClipSpec.Progress(start, refresh.start),
            initialProgress = start,
        )
    }
    animate(
        composition,
        iteration = 1,
        iterations = LottieConstants.IterateForever,
        reverseOnRepeat = false,
        clipSpec = LottieClipSpec.Progress(refresh.start, refresh.endInclusive),
        initialProgress = start.coerceIn(refresh.start, refresh.endInclusive),
    )
}

/**
 * Plays the Lottie animation at the APK asset path the options name: its
 * `pull` segment as the list is pulled, its `refresh` segment in a loop
 * while refreshing. mobile-ui draws the disc around it.
 */
@Composable
fun LottieRefreshIndicator(scope: RefreshIndicatorScope) {
    val asset = lottieRefreshIndicatorAsset(scope.options, isSystemInDarkTheme()) ?: return
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(asset))
    val segments = remember(composition) { composition?.refreshSegments() ?: LottieRefreshSegments.Whole }
    val loop = rememberLottieAnimatable()
    LaunchedEffect(composition, scope.isRefreshing) {
        val loaded = composition
        if (scope.isRefreshing && loaded != null) {
            loop.playRefreshLoop(loaded, segments, scope.progress())
        }
    }

    LottieAnimation(
        composition,
        progress = { lottieRefreshIndicatorProgress(scope.progress(), scope.isRefreshing, loop.isPlaying, loop.progress, segments) },
        modifier = Modifier
            .fillMaxSize()
            .testTag(LOTTIE_REFRESH_INDICATOR_TAG)
            .semantics {
                progressBarRangeInfo = if (scope.isRefreshing) ProgressBarRangeInfo.Indeterminate
                else ProgressBarRangeInfo(scope.progress().coerceIn(0f, 1f), 0f..1f)
            },
    )
}
