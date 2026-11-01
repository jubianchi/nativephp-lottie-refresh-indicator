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
 * The parts of the animation, as progress ranges: [pull] plays as the list
 * is pulled, [refresh] loops forward while it refreshes. Without a refresh
 * segment, the refresh plays the pull segment back and forth instead.
 */
data class LottieRefreshSegments(
    val pull: ClosedFloatingPointRange<Float>,
    val refresh: ClosedFloatingPointRange<Float>?,
) {
    fun pullProgress(pull: Float): Float =
        this.pull.start + pull.coerceIn(0f, 1f) * (this.pull.endInclusive - this.pull.start)

    companion object {
        val Whole = LottieRefreshSegments(0f..1f, null)
    }
}

/**
 * The segments the `pull` and `refresh` markers give, either missing: no
 * `pull` marker leaves what comes before `refresh` to the pull, or the
 * whole animation when nothing does. Empty markers count as missing.
 */
fun lottieRefreshSegments(pull: ClosedFloatingPointRange<Float>?, refresh: ClosedFloatingPointRange<Float>?): LottieRefreshSegments {
    val refreshSegment = refresh?.takeIf { it.endInclusive > it.start }
    val pullSegment = pull?.takeIf { it.endInclusive > it.start }
        ?: refreshSegment?.takeIf { it.start > 0f }?.let { 0f..it.start }
        ?: 0f..1f

    return LottieRefreshSegments(pullSegment, refreshSegment)
}

/**
 * Where the animation stands: following the pull, or the refresh loop once
 * it is actually running, so releasing never jumps to a blank frame.
 */
fun lottieRefreshIndicatorProgress(
    pull: Float,
    isRefreshing: Boolean,
    loopIsPlaying: Boolean,
    loopProgress: Float,
    segments: LottieRefreshSegments = LottieRefreshSegments.Whole,
): Float =
    if (isRefreshing && loopIsPlaying) loopProgress else segments.pullProgress(pull)
