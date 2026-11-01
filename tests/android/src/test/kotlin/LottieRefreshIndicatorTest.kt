package com.jubianchi.nativephp.lottierefreshindicator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.nativephp.plugins.native_ui.NativeUIRefreshIndicators
import com.nativephp.plugins.native_ui.RefreshIndicatorScope
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LottieRefreshIndicatorTest {
    @get:Rule val compose = createComposeRule()

    @Before fun register() = registerLottieRefreshIndicator(ApplicationProvider.getApplicationContext())

    @After fun forgetIndicators() = NativeUIRefreshIndicators.reset()

    @Test fun theInitFunctionRegistersTheLottieIndicator() {
        assertNotNull(NativeUIRefreshIndicators.find("lottie"))
    }

    @Test fun anIndicatorWithoutAnAssetDrawsNothing() {
        show(RefreshIndicatorScope({ 0.5f }, isRefreshing = false, options = emptyMap()))

        compose.onAllNodesWithTag(LOTTIE_REFRESH_INDICATOR_TAG).assertCountEquals(0)
    }

    @Test fun aPulledIndicatorReportsThePull() {
        show(RefreshIndicatorScope({ 0.4f }, isRefreshing = false, options = mapOf("asset" to "animations/splash.lottie")))

        compose.onNodeWithTag(LOTTIE_REFRESH_INDICATOR_TAG)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0.4f, 0f..1f)))
    }

    @Test fun aRefreshingIndicatorReportsAnIndeterminateProgress() {
        show(RefreshIndicatorScope({ 1f }, isRefreshing = true, options = mapOf("asset" to "animations/splash.lottie")))

        compose.onNodeWithTag(LOTTIE_REFRESH_INDICATOR_TAG)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate))
    }

    private fun show(scope: RefreshIndicatorScope) {
        val indicator = NativeUIRefreshIndicators.find("lottie")!!
        compose.setContent { Box(Modifier.size(36.dp)) { indicator(scope) } }
        compose.waitForIdle()
    }
}
