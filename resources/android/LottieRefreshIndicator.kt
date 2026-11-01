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

/**
 * Plays the Lottie animation at the APK asset path the options name: it
 * draws in as the list is pulled and loops while refreshing. mobile-ui
 * draws the disc around it.
 */
@Composable
fun LottieRefreshIndicator(scope: RefreshIndicatorScope) {
    val asset = lottieRefreshIndicatorAsset(scope.options, isSystemInDarkTheme()) ?: return
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(asset))
    val loop = rememberLottieAnimatable()
    LaunchedEffect(composition, scope.isRefreshing) {
        val loaded = composition
        if (scope.isRefreshing && loaded != null) {
            loop.animate(
                loaded,
                iterations = LottieConstants.IterateForever,
                initialProgress = scope.progress().coerceIn(0f, 1f),
            )
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
