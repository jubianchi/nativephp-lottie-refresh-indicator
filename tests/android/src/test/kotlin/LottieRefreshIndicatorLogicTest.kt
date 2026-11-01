package com.jubianchi.nativephp.lottierefreshindicator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LottieRefreshIndicatorLogicTest {
    @Test fun theLightModeShowsTheAsset() {
        val options = mapOf("asset" to "animations/splash.lottie", "dark_asset" to "animations/splash-dark.lottie")

        assertEquals("animations/splash.lottie", lottieRefreshIndicatorAsset(options, dark = false))
    }

    @Test fun theDarkModeShowsTheDarkAsset() {
        val options = mapOf("asset" to "animations/splash.lottie", "dark_asset" to "animations/splash-dark.lottie")

        assertEquals("animations/splash-dark.lottie", lottieRefreshIndicatorAsset(options, dark = true))
    }

    @Test fun theDarkModeFallsBackToTheAssetWithoutADarkOne() {
        assertEquals("a.lottie", lottieRefreshIndicatorAsset(mapOf("asset" to "a.lottie"), dark = true))
        assertEquals("a.lottie", lottieRefreshIndicatorAsset(mapOf("asset" to "a.lottie", "dark_asset" to ""), dark = true))
        assertEquals("a.lottie", lottieRefreshIndicatorAsset(mapOf("asset" to "a.lottie", "dark_asset" to 3), dark = true))
    }

    @Test fun thereIsNothingToShowWithoutAnAsset() {
        assertNull(lottieRefreshIndicatorAsset(emptyMap(), dark = false))
        assertNull(lottieRefreshIndicatorAsset(mapOf("asset" to ""), dark = false))
        assertNull(lottieRefreshIndicatorAsset(mapOf("asset" to null), dark = false))
        assertNull(lottieRefreshIndicatorAsset(mapOf("asset" to listOf("a.lottie")), dark = false))
        assertNull(lottieRefreshIndicatorAsset(mapOf("dark_asset" to "dark.lottie"), dark = true))
    }

    @Test fun thePullDrivesTheAnimation() {
        assertEquals(0.4f, lottieRefreshIndicatorProgress(0.4f, isRefreshing = false, loopIsPlaying = false, loopProgress = 0.9f))
    }

    @Test fun thePullIsClamped() {
        assertEquals(0f, lottieRefreshIndicatorProgress(-0.2f, isRefreshing = false, loopIsPlaying = false, loopProgress = 0f))
        assertEquals(1f, lottieRefreshIndicatorProgress(1.6f, isRefreshing = false, loopIsPlaying = false, loopProgress = 0f))
    }

    @Test fun thePullStaysUntilTheLoopRuns() {
        assertEquals(1f, lottieRefreshIndicatorProgress(1f, isRefreshing = true, loopIsPlaying = false, loopProgress = 0f))
    }

    @Test fun theRunningLoopDrivesTheAnimationWhileRefreshing() {
        assertEquals(0.25f, lottieRefreshIndicatorProgress(1f, isRefreshing = true, loopIsPlaying = true, loopProgress = 0.25f))
    }

    @Test fun aLoopStillWindingDownAfterTheRefreshLeavesTheAnimationToThePull() {
        assertEquals(0.3f, lottieRefreshIndicatorProgress(0.3f, isRefreshing = false, loopIsPlaying = true, loopProgress = 0.8f))
    }
}
