package co.elastic.otel.android.test

import co.elastic.otel.android.test.rule.AndroidTestAgentRule
import org.assertj.core.api.Assertions.assertThat
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

        assertThat(testUncaughtExceptionHandler.getUncaughtExceptions()).containsExactly(exception)
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