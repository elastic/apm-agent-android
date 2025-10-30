package co.elastic.otel.android.test

import co.elastic.otel.android.test.rule.AndroidTestAgentRule
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test

class InstrumentationTest {
    private val testUncaughtExceptionHandler = TestUncaughtExceptionHandler()

    @get:Rule
    val agentRule = AndroidTestAgentRule {
        Thread.setDefaultUncaughtExceptionHandler(testUncaughtExceptionHandler)
    }

    @After
    fun tearDown() {
        testUncaughtExceptionHandler.reset()
    }

    @Test
    fun testCrashInstrumentation() {
        val exception = RuntimeException("My exception")

        Thread.getDefaultUncaughtExceptionHandler()!!
            .uncaughtException(Thread.currentThread(), exception)

        agentRule.flushLogs()

        assertThat(testUncaughtExceptionHandler.getUncaughtExceptions()).containsExactly(exception)
        assertThat(agentRule.getFinishedLogRecords()).hasSize(1)
        assertThat(agentRule.getFinishedLogRecords().first().attributes)
            .containsEntry("event.name", "device.crash")
            .containsEntry("exception.message", "My exception")
            .containsEntry("exception.type", RuntimeException::class.java.name)
            .containsKey("exception.stacktrace")
    }

    private class TestUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
        private val exceptions = mutableListOf<Throwable>()

        override fun uncaughtException(p0: Thread, p1: Throwable) {
            exceptions.add(p1)
        }

        fun getUncaughtExceptions(): List<Throwable> {
            return exceptions
        }

        fun reset() {
            exceptions.clear()
        }
    }
}