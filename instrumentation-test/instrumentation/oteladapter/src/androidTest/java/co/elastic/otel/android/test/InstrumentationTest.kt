package co.elastic.otel.android.test

import androidx.test.core.app.launchActivity
import co.elastic.otel.android.test.rule.AndroidTestAgentRule
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import java.util.concurrent.TimeUnit
import org.junit.Rule
import org.junit.Test

class InstrumentationTest {

    @get:Rule
    val agentRule = AndroidTestAgentRule()

    @Test
    fun verifyAndroidLogsAreInstrumented() {
        launchActivity<MainActivity>().onActivity {
            agentRule.flushLogs().join(5, TimeUnit.SECONDS)

            val finishedLogRecords = agentRule.getFinishedLogRecords()
            assertThat(finishedLogRecords).hasSize(1)
            assertThat(finishedLogRecords.first())
                .hasBody("My log")
                .hasAttributesSatisfying {
                    assertThat(it.get(AttributeKey.stringKey("android.log.tag"))).isEqualTo("elastic")
                }
        }
    }
}