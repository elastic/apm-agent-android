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
package co.elastic.otel.instrumentation.crash.internal

import co.elastic.otel.api.ElasticAgent
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.semconv.SemanticAttributes
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.TimeUnit

class ElasticExceptionHandler internal constructor(
    private val agent: ElasticAgent,
    private val wrapped: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        emitCrashEvent(e)
        agent.flushLogRecords().join(5, TimeUnit.SECONDS)

        wrapped?.uncaughtException(t, e)
    }

    private fun emitCrashEvent(e: Throwable) {
        agent.getOpenTelemetry().logsBridge["CrashEvents"].logRecordBuilder()
            .setAllAttributes(
                Attributes.builder()
                    .put(SemanticAttributes.EXCEPTION_MESSAGE, e.message)
                    .put(SemanticAttributes.EXCEPTION_STACKTRACE, stackTraceToString(e))
                    .put(SemanticAttributes.EXCEPTION_TYPE, e.javaClass.name)
                    .build()
            ).emit()
    }

    private fun stackTraceToString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        throwable.printStackTrace(pw)
        pw.flush()

        return sw.toString()
    }
}
