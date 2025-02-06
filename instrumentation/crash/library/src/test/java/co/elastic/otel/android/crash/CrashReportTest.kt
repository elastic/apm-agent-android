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
package co.elastic.otel.android.crash

import co.elastic.otel.android.test.common.ElasticAttributes.getLogRecordDefaultAttributes
import co.elastic.otel.android.test.rule.RobolectricAgentRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CrashReportTest {

    @get:Rule
    val agentRule = RobolectricAgentRule()

    @Test
    fun `Capture log event with crash`() {
        val exception: Exception = IllegalStateException("Custom exception")
        throwException(exception)

        val logs = agentRule.getFinishedLogRecords()

        assertThat(logs).hasSize(1)
        assertThat(logs.first()).hasAttributes(
            Attributes.builder().putAll(getLogRecordDefaultAttributes())
                .put("event.name", "crash")
                .put("event.domain", "device")
                .put("exception.message", "Custom exception")
                .put("exception.stacktrace", exception.stackTraceToString())
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

        val elasticHandler = throwException(exception)

        assertThat(originalExceptionHandler).isNotEqualTo(elasticHandler)
        verify { originalExceptionHandler.uncaughtException(Thread.currentThread(), exception) }
    }

    private fun throwException(exception: Exception = IllegalStateException("Custom exception")): Thread.UncaughtExceptionHandler? {
        val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        exceptionHandler?.uncaughtException(Thread.currentThread(), exception)
        return exceptionHandler
    }
}
