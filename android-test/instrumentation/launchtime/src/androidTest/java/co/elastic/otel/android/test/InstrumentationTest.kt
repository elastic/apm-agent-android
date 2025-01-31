package co.elastic.otel.android.test

import androidx.test.core.app.launchActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class InstrumentationTest {
    @get:Rule
    val agentRule = AgentRule()

    @Test
    fun verifyAppLaunchTimeTracking() {
        launchActivity<MainActivity>().use {
            val finishedMetrics = agentRule.getFinishedMetrics()
            assertThat(finishedMetrics).hasSize(1)
        }
    }
}