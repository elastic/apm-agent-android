/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.sdk.internal.features

import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration
import co.elastic.apm.android.sdk.testutils.ElasticAgentRule
import co.elastic.apm.android.sdk.testutils.ElasticAgentRule.Companion.LOG_DEFAULT_ATTRS
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import java.io.PrintWriter
import java.io.StringWriter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CrashReportTest {

    @get:Rule
    val agentRule = ElasticAgentRule()

    @Test
    fun `Capture log event with crash`() {
        agentRule.initialize()
        val exception: Exception = IllegalStateException("Custom exception")
        throwException(exception)

        val logs = agentRule.getFinishedLogRecords()

        assertThat(logs).hasSize(1)
        assertThat(logs.first()).hasAttributes(
            Attributes.builder().putAll(LOG_DEFAULT_ATTRS)
                .put("event.name", "crash")
                .put("event.domain", "device")
                .put("exception.message", "Custom exception")
                .put("exception.stacktrace", stackTraceToString(exception))
                .put("exception.type", "java.lang.IllegalStateException")
                .build()
        )
    }

    @Test
    fun `Delegate to existing exception handler when available`() {
        val originalExceptionHandler: Thread.UncaughtExceptionHandler = mockk()
        every { originalExceptionHandler.uncaughtException(any(), any()) } just Runs
        Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler)
        val exception = IllegalStateException("Custom exception")
        agentRule.initialize()

        val elasticHandler = throwException(exception)

        assertThat(originalExceptionHandler).isNotEqualTo(elasticHandler)
        verify { originalExceptionHandler.uncaughtException(Thread.currentThread(), exception) }
    }

    @Test
    fun whenInstrumentationIsDisabled_doNotSendCrashReport() {
        agentRule.initialize(configurationInterceptor = {
            it.setInstrumentationConfiguration(
                InstrumentationConfiguration.builder().enableCrashReporting(false).build()
            )
        })

        throwException()

        assertThat(agentRule.getFinishedLogRecords()).hasSize(0)
    }

    private fun throwException(exception: Exception = IllegalStateException("Custom exception")): Thread.UncaughtExceptionHandler? {
        val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        exceptionHandler?.uncaughtException(Thread.currentThread(), exception)
        return exceptionHandler
    }

    private fun stackTraceToString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        throwable.printStackTrace(pw)
        pw.flush()

        return sw.toString()
    }
}
