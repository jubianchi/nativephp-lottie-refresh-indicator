package com.jubianchi.nativephp.lottierefreshindicator

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimatable
import com.airbnb.lottie.compose.LottieAnimation
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
 * Loops back and forth from [from], the pull at release, so the animation
 * never jumps: a release happens at the end of the pull, and wrapping from
 * the end to the start would blank a draw-in logo.
 */
suspend fun LottieAnimatable.playRefreshLoop(composition: LottieComposition, from: Float) {
    animate(
        composition,
        iterations = LottieConstants.IterateForever,
        reverseOnRepeat = true,
        initialProgress = from.coerceIn(0f, 1f),
    )
}

@Composable
fun LottieRefreshIndicator(scope: RefreshIndicatorScope) {
    val asset = lottieRefreshIndicatorAsset(scope.options, isSystemInDarkTheme()) ?: return
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(asset))
    val loop = rememberLottieAnimatable()
    LaunchedEffect(composition, scope.isRefreshing) {
        val loaded = composition
        if (scope.isRefreshing && loaded != null) {
            loop.playRefreshLoop(loaded, scope.progress())
        }
    }

    LottieAnimation(
        composition,
        progress = { lottieRefreshIndicatorProgress(scope.progress(), scope.isRefreshing, loop.isPlaying, loop.progress) },
        modifier = Modifier
            .fillMaxSize()
            .testTag(LOTTIE_REFRESH_INDICATOR_TAG)
            .semantics {
                progressBarRangeInfo = if (scope.isRefreshing) ProgressBarRangeInfo.Indeterminate
                else ProgressBarRangeInfo(scope.progress().coerceIn(0f, 1f), 0f..1f)
            },
    )
}
