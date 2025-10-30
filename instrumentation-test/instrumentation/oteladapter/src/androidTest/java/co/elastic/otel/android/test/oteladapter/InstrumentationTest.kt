package co.elastic.otel.android.test.oteladapter

import androidx.test.core.app.launchActivity
import co.elastic.otel.android.test.rule.AndroidTestAgentRule
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions
import java.util.concurrent.TimeUnit
import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test

class InstrumentationTest {

    @get:Rule
    val agentRule = AndroidTestAgentRule()

    @Test
    fun verifyAndroidLogsAreInstrumented() {
        launchActivity<MainActivity>().onActivity { activity ->
            activity.sendLog()

            agentRule.flushLogs().join(5, TimeUnit.SECONDS)

            val finishedLogRecords = agentRule.getFinishedLogRecords()
            Assertions.assertThat(finishedLogRecords).hasSize(1)
            OpenTelemetryAssertions.assertThat(finishedLogRecords.first())
                .hasBody("My log")
                .hasAttributesSatisfying {
                    Assertions.assertThat(it.get(AttributeKey.stringKey("android.log.tag")))
                        .isEqualTo("elastic")
                }

            // Closing instrumentation
            agentRule.getInstrumentationManager().close()

            activity.sendLog()

            agentRule.flushLogs().join(5, TimeUnit.SECONDS)
            Assertions.assertThat(agentRule.getFinishedLogRecords()).isEmpty()
        }
    }
}