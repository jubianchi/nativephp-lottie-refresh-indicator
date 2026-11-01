package com.jubianchi.nativephp.lottierefreshindicator

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimatable
import com.airbnb.lottie.compose.rememberLottieAnimatable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LottieRefreshLoopTest {
    @get:Rule val compose = createComposeRule()

    private lateinit var loop: LottieAnimatable

    private lateinit var segments: LottieRefreshSegments

    private var from by mutableFloatStateOf(-1f)

    @Test fun withoutMarkersALoopFromAFullPullPlaysBackwardWithoutBlanking() {
        start(1f, composition())

        val frames = frames(90)

        assertEquals(1f, frames.first(), 0.02f)
        assertTrue("left the end of the animation: ${frames.take(10)}", frames.take(10).all { it > 0.8f })
        assertContinuous(frames)
    }

    @Test fun withoutMarkersALoopFromAPartialPullCarriesOnFromIt() {
        start(0.4f, composition())

        val frames = frames(240)

        assertEquals(0.4f, frames.first(), 0.02f)
        assertContinuous(frames)
        assertTrue("never reached the end: ${frames.max()}", frames.max() > 0.98f)
    }

    @Test fun withoutMarkersALoopKeepsGoingBackAndForth() {
        start(1f, composition())

        val frames = frames(300)

        assertContinuous(frames)
        assertTrue("never reached the start: ${frames.min()}", frames.min() < 0.02f)
        assertTrue("never came back to the end: ${frames.drop(150).max()}", frames.drop(150).max() > 0.98f)
    }

    @Test fun withoutMarkersALaterRefreshStartsWithoutBlankingEither() {
        start(1f, composition())
        frames(200)

        restart(0.99f)

        val frames = frames(30)
        assertEquals("frames: $frames", 0.99f, frames.first(), 0.02f)
        assertTrue("left the end of the animation: ${frames.take(10)}", frames.take(10).all { it > 0.8f })
        assertContinuous(frames)
    }

    @Test fun aFullPullLoopsTheRefreshMarkerForward() {
        start(1f, composition(PULL, REFRESH))

        val frames = frames(250)

        assertEquals(0.5f, frames.first(), 0.01f)
        assertForwardOverRefresh(frames)
        assertTrue("went before the refresh marker: ${frames.min()}", frames.min() > 0.49f)
    }

    @Test fun aPartialPullFinishesThePullMarkerThenLoopsTheRefreshMarker() {
        start(0.9f, composition(PULL, REFRESH))

        val frames = frames(250)

        assertEquals(0.45f, frames.first(), 0.01f)
        assertForwardOverRefresh(frames)
        val inRefresh = frames.indexOfFirst { it >= 0.5f }
        assertTrue("went back before the refresh marker: ${frames.drop(inRefresh).min()}", frames.drop(inRefresh).min() > 0.49f)
    }

    @Test fun aRefreshMarkerAloneLoopsForwardAfterThePull() {
        start(1f, composition(REFRESH))

        val frames = frames(250)

        assertEquals(0.5f, frames.first(), 0.01f)
        assertForwardOverRefresh(frames)
    }

    @Test fun aPullMarkerAloneGoesBackAndForthOverIt() {
        start(1f, composition(PULL))

        val frames = frames(300)

        assertEquals(0.5f, frames.first(), 0.01f)
        assertContinuous(frames)
        assertTrue("never reached the start: ${frames.min()}", frames.min() < 0.02f)
        assertTrue("went past the pull marker: ${frames.max()}", frames.max() < 0.51f)
    }

    private fun start(progress: Float, composition: LottieComposition) {
        compose.mainClock.autoAdvance = false
        segments = composition.refreshSegments()
        from = progress
        compose.setContent {
            loop = rememberLottieAnimatable()
            LaunchedEffect(from) { if (from >= 0f) loop.playRefreshLoop(composition, segments, from) }
        }
        compose.mainClock.advanceTimeByFrame()
    }

    private fun restart(progress: Float) {
        from = progress
        compose.mainClock.advanceTimeByFrame()
    }

    private fun frames(count: Int): List<Float> = List(count) {
        compose.mainClock.advanceTimeByFrame()
        lottieRefreshIndicatorProgress(from, isRefreshing = true, loop.isPlaying, loop.progress, segments)
    }

    private fun assertContinuous(frames: List<Float>) {
        frames.zipWithNext().forEachIndexed { index, (a, b) ->
            assertTrue("jumped from $a to $b at frame $index", abs(b - a) < 0.05f)
        }
    }

    /** Only ever forward, by small steps, but for the seam from the end of the refresh marker back to its start. */
    private fun assertForwardOverRefresh(frames: List<Float>) {
        var seams = 0
        frames.zipWithNext().forEachIndexed { index, (a, b) ->
            if (a > 0.97f && b < 0.53f) {
                seams++
            } else {
                assertTrue("went from $a to $b at frame $index", b >= a && b - a < 0.05f)
            }
        }
        assertTrue("looped $seams times", seams >= 3)
    }
}
