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
package co.elastic.otel.android.crash.internal.handler

import android.app.Application
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
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ElasticExceptionHandlerTest {

    @get:Rule
    val agentRule = RobolectricAgentRule()

    @Test
    fun `Capture log event with crash`() {
        val exception = throwException()

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

    @Config(application = ExistingExceptionHandlerApp::class)
    @Test
    fun `Delegate to existing exception handler when available`() {
        val app = RuntimeEnvironment.getApplication() as ExistingExceptionHandlerApp

        val exception = throwException()

        assertThat(agentRule.getFinishedLogRecords()).hasSize(1)
        verify { app.existingHandler.uncaughtException(Thread.currentThread(), exception) }
    }

    private class ExistingExceptionHandlerApp : Application() {
        lateinit var existingHandler: Thread.UncaughtExceptionHandler

        override fun onCreate() {
            super.onCreate()
            existingHandler = mockk()
            Thread.setDefaultUncaughtExceptionHandler(existingHandler)
            every { existingHandler.uncaughtException(any(), any()) } just Runs
        }
    }

    private fun throwException(exception: Exception = IllegalStateException("Custom exception")): Exception {
        Thread.getDefaultUncaughtExceptionHandler()
            ?.uncaughtException(Thread.currentThread(), exception)
        return exception
    }
}
