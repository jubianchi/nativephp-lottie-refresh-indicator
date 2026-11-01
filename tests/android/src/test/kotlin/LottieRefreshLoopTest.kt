package com.jubianchi.nativephp.lottierefreshindicator

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
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

    private val composition: LottieComposition =
        LottieCompositionFactory.fromJsonStringSync("""{"v":"5.7.4","fr":30,"ip":0,"op":60,"w":10,"h":10,"layers":[]}""", null).value!!

    private lateinit var loop: LottieAnimatable

    private var from by mutableFloatStateOf(-1f)

    @Test fun aLoopStartedFromAFullPullPlaysBackwardWithoutBlanking() {
        start(1f)

        val frames = frames(90)

        assertEquals(1f, frames.first(), 0.02f)
        assertTrue("left the end of the animation: ${frames.take(10)}", frames.take(10).all { it > 0.8f })
        assertContinuous(frames)
    }

    @Test fun aLoopStartedFromAPartialPullCarriesOnFromIt() {
        start(0.4f)

        val frames = frames(240)

        assertEquals(0.4f, frames.first(), 0.02f)
        assertContinuous(frames)
        assertTrue("never reached the end: ${frames.max()}", frames.max() > 0.98f)
    }

    @Test fun aLoopKeepsGoingBackAndForth() {
        start(1f)

        val frames = frames(300)

        assertContinuous(frames)
        assertTrue("never reached the start: ${frames.min()}", frames.min() < 0.02f)
        assertTrue("never came back to the end: ${frames.drop(150).max()}", frames.drop(150).max() > 0.98f)
    }

    @Test fun aLaterRefreshStartsWithoutBlankingEither() {
        start(1f)
        frames(200)

        restart(0.99f)

        val frames = frames(30)
        assertEquals("frames: $frames", 0.99f, frames.first(), 0.02f)
        assertTrue("left the end of the animation: ${frames.take(10)}", frames.take(10).all { it > 0.8f })
        assertContinuous(frames)
    }

    private fun start(progress: Float) {
        compose.mainClock.autoAdvance = false
        from = progress
        compose.setContent {
            loop = rememberLottieAnimatable()
            LaunchedEffect(from) { if (from >= 0f) loop.playRefreshLoop(composition, from) }
        }
        compose.mainClock.advanceTimeByFrame()
    }

    private fun restart(progress: Float) {
        from = progress
        compose.mainClock.advanceTimeByFrame()
    }

    private fun frames(count: Int): List<Float> = List(count) {
        compose.mainClock.advanceTimeByFrame()
        loop.progress
    }

    private fun assertContinuous(frames: List<Float>) {
        frames.zipWithNext().forEachIndexed { index, (a, b) ->
            assertTrue("jumped from $a to $b at frame $index", abs(b - a) < 0.05f)
        }
    }
}
