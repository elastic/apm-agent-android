package co.elastic.otel.android.test.crash

import co.elastic.otel.android.test.rule.AndroidTestAgentRule
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions
import org.assertj.core.api.Assertions
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

        Assertions.assertThat(testUncaughtExceptionHandler.getUncaughtExceptions()).containsExactly(exception)
        val finishedLogRecords = agentRule.getFinishedLogRecords()
        Assertions.assertThat(finishedLogRecords).hasSize(1)
        OpenTelemetryAssertions.assertThat(finishedLogRecords.first().attributes)
            .containsEntry("event.name", "device.crash")
            .containsEntry("exception.message", "My exception")
            .containsEntry("exception.type", RuntimeException::class.java.name)
            .containsKey("exception.stacktrace")
    }

    @Test
    fun testCrashInstrumentation_uninstall() {
        val exception = RuntimeException("My exception")

        agentRule.closeAgent()

        Thread.getDefaultUncaughtExceptionHandler()!!
            .uncaughtException(Thread.currentThread(), exception)

        Assertions.assertThat(testUncaughtExceptionHandler.getUncaughtExceptions()).containsExactly(exception)
        Assertions.assertThat(agentRule.getFinishedLogRecords()).isEmpty()
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