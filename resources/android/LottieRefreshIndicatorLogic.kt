package com.jubianchi.nativephp.lottierefreshindicator

/**
 * The animation the indicator plays: `dark_asset` in dark mode when given,
 * `asset` otherwise; none without an `asset`.
 */
fun lottieRefreshIndicatorAsset(options: Map<String, Any?>, dark: Boolean): String? {
    val asset = (options["asset"] as? String)?.takeIf { it.isNotEmpty() } ?: return null
    val darkAsset = (options["dark_asset"] as? String)?.takeIf { it.isNotEmpty() }

    return if (dark && darkAsset != null) darkAsset else asset
}

/**
 * Where the animation stands: following the pull, or the refresh loop once
 * it is actually running, so releasing never jumps to a blank frame.
 */
fun lottieRefreshIndicatorProgress(pull: Float, isRefreshing: Boolean, loopIsPlaying: Boolean, loopProgress: Float): Float =
    if (isRefreshing && loopIsPlaying) loopProgress else pull.coerceIn(0f, 1f)
