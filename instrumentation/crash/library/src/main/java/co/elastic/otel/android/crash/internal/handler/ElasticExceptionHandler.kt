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

import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.api.clock.ClockProvider
import co.elastic.otel.android.api.flusher.LogRecordFlusher
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.incubator.events.EventBuilder
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider
import io.opentelemetry.semconv.ExceptionAttributes
import io.opentelemetry.semconv.SemanticAttributes.EVENT_DOMAIN
import java.util.concurrent.TimeUnit

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
class ElasticExceptionHandler internal constructor(
    private val agent: ElasticOtelAgent,
    private val crashEventBuilder: EventBuilder,
    private val delegate: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        emitCrashEvent(e)

        if (agent is LogRecordFlusher) {
            agent.flushLogRecords().join(5, TimeUnit.SECONDS)
        }

        delegate?.uncaughtException(t, e)
    }

    private fun emitCrashEvent(e: Throwable) {
        crashEventBuilder.setAttributes(
            Attributes.builder()
                .put(ExceptionAttributes.EXCEPTION_MESSAGE, e.message)
                .put(ExceptionAttributes.EXCEPTION_STACKTRACE, e.stackTraceToString())
                .put(ExceptionAttributes.EXCEPTION_TYPE, e.javaClass.name)
                .build()
        ).emit()
    }

    companion object {
        internal fun create(agent: ElasticOtelAgent): ElasticExceptionHandler {
            val eventLoggerProvider = if (agent is ClockProvider) {
                SdkEventLoggerProvider.create(agent.getOpenTelemetry().logsBridge, agent.getClock())
            } else {
                SdkEventLoggerProvider.create(agent.getOpenTelemetry().logsBridge)
            }
            val eventBuilder = eventLoggerProvider.get("CrashReport").builder("crash")
                .setAttributes(Attributes.of(EVENT_DOMAIN, "device"))
            return ElasticExceptionHandler(
                agent,
                eventBuilder,
                Thread.getDefaultUncaughtExceptionHandler()
            )
        }
    }
}