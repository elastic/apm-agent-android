package co.elastic.otel.android.test

import co.elastic.otel.android.test.rule.AndroidTestAgentRule
import org.junit.Rule
import org.junit.Test

class InstrumentationTest {
    @get:Rule
    val agentRule = AndroidTestAgentRule()

    @Test
    fun verifyOkhttpSyncSpan() {
        TODO()
    }
}