package com.jubianchi.nativephp.lottierefreshindicator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LottieRefreshSegmentsTest {
    @Test fun bothMarkersSplitTheAnimation() {
        val segments = lottieRefreshSegments(pull = 0f..0.5f, refresh = 0.5f..1f)

        assertEquals(0f..0.5f, segments.pull)
        assertEquals(0.5f..1f, segments.refresh)
    }

    @Test fun withoutMarkersThePullIsTheWholeAnimationAndThereIsNoRefreshSegment() {
        val segments = lottieRefreshSegments(pull = null, refresh = null)

        assertEquals(0f..1f, segments.pull)
        assertNull(segments.refresh)
    }

    @Test fun aPullMarkerAloneLeavesNoRefreshSegment() {
        val segments = lottieRefreshSegments(pull = 0f..0.5f, refresh = null)

        assertEquals(0f..0.5f, segments.pull)
        assertNull(segments.refresh)
    }

    @Test fun aRefreshMarkerAloneLeavesWhatComesBeforeItToThePull() {
        val segments = lottieRefreshSegments(pull = null, refresh = 0.5f..1f)

        assertEquals(0f..0.5f, segments.pull)
        assertEquals(0.5f..1f, segments.refresh)
    }

    @Test fun aRefreshMarkerFromTheStartLeavesTheWholeAnimationToThePull() {
        val segments = lottieRefreshSegments(pull = null, refresh = 0f..1f)

        assertEquals(0f..1f, segments.pull)
        assertEquals(0f..1f, segments.refresh)
    }

    @Test fun emptyMarkersAreIgnored() {
        val segments = lottieRefreshSegments(pull = 0.2f..0.2f, refresh = 0.5f..0.5f)

        assertEquals(0f..1f, segments.pull)
        assertNull(segments.refresh)
    }

    @Test fun thePullMapsOntoThePullSegment() {
        val segments = lottieRefreshSegments(pull = 0f..0.5f, refresh = 0.5f..1f)

        assertEquals(0f, segments.pullProgress(0f))
        assertEquals(0.25f, segments.pullProgress(0.5f))
        assertEquals(0.5f, segments.pullProgress(1f))
        assertEquals(0.5f, segments.pullProgress(1.4f))
        assertEquals(0f, segments.pullProgress(-0.3f))
    }

    @Test fun theIndicatorShowsThePullOnThePullSegment() {
        val segments = lottieRefreshSegments(pull = 0.2f..0.6f, refresh = 0.6f..1f)

        assertEquals(0.4f, lottieRefreshIndicatorProgress(0.5f, isRefreshing = false, loopIsPlaying = false, loopProgress = 0f, segments = segments), 0.0001f)
        assertEquals(0.6f, lottieRefreshIndicatorProgress(1f, isRefreshing = true, loopIsPlaying = false, loopProgress = 0f, segments = segments), 0.0001f)
        assertEquals(0.9f, lottieRefreshIndicatorProgress(1f, isRefreshing = true, loopIsPlaying = true, loopProgress = 0.9f, segments = segments), 0.0001f)
    }
}
