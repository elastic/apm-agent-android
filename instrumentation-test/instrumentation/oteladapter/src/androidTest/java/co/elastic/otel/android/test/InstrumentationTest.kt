package co.elastic.otel.android.test

import android.util.Log
import co.elastic.otel.android.test.rule.AndroidTestAgentRule
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import java.util.concurrent.TimeUnit
import org.junit.Rule
import org.junit.Test

class InstrumentationTest {

    @get:Rule
    val agentRule = AndroidTestAgentRule()

    @Test
    fun verifyAndroidLogsAreInstrumented() {
        Log.d("elastic", "My log")

        agentRule.flushLogs().join(5, TimeUnit.SECONDS)

        assertThat(agentRule.getFinishedLogRecords()).hasSize(1)
    }
}