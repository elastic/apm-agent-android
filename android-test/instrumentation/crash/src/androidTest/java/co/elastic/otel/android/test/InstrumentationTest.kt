package co.elastic.otel.android.test

import co.elastic.otel.android.crash.internal.handler.ElasticExceptionHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class InstrumentationTest {
    @get:Rule
    val agentRule = AgentRule()

    @Test
    fun verifyAppCrashDefaultHandler() {
        assertThat(Thread.getDefaultUncaughtExceptionHandler()).isInstanceOf(ElasticExceptionHandler::class.java)
    }
}