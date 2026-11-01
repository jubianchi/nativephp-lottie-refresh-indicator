package com.jubianchi.nativephp.lottierefreshindicator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LottieRefreshMarkersTest {
    @Test fun readsThePullAndRefreshMarkers() {
        assertSegments(0f..0.5f, 0.5f..1f, composition(PULL, REFRESH).refreshSegments())
    }

    @Test fun readsAnAnimationWithoutMarkers() {
        assertSegments(0f..1f, null, composition().refreshSegments())
    }

    @Test fun readsAnAnimationWithAPullMarkerOnly() {
        assertSegments(0f..0.5f, null, composition(PULL).refreshSegments())
    }

    @Test fun readsAnAnimationWithARefreshMarkerOnly() {
        assertSegments(0f..0.5f, 0.5f..1f, composition(REFRESH).refreshSegments())
    }

    @Test fun ignoresOtherMarkers() {
        assertSegments(0f..1f, null, composition(Triple("intro", 0, 30)).refreshSegments())
    }

    private fun assertSegments(
        pull: ClosedFloatingPointRange<Float>,
        refresh: ClosedFloatingPointRange<Float>?,
        actual: LottieRefreshSegments,
    ) {
        assertRange(pull, actual.pull)
        if (refresh == null) assertNull(actual.refresh) else assertRange(refresh, actual.refresh!!)
    }

    private fun assertRange(expected: ClosedFloatingPointRange<Float>, actual: ClosedFloatingPointRange<Float>) {
        assertEquals("start of $actual", expected.start, actual.start, 0.001f)
        assertEquals("end of $actual", expected.endInclusive, actual.endInclusive, 0.001f)
        assertEquals("end of $actual within the animation", true, actual.endInclusive <= 1f)
    }
}
