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
package co.elastic.otel.android.extensions

import co.elastic.otel.android.api.ElasticOtelAgent
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope
import java.time.Instant

private const val SCOPE_NAME = "co.elastic.otel.android.extensions"

fun ElasticOtelAgent.log(
    body: String,
    severity: Severity? = null,
    severityText: String? = null,
    attributes: Attributes? = null,
    context: Context? = null,
    observedTimestamp: Instant? = null,
    timestamp: Instant? = null
) {
    val logger = getOpenTelemetry().logsBridge.get(SCOPE_NAME).logRecordBuilder()
        .setBody(body)
    severity?.let { logger.setSeverity(it) }
    severityText?.let { logger.setSeverityText(it) }
    attributes?.let { logger.setAllAttributes(it) }
    context?.let { logger.setContext(it) }
    observedTimestamp?.let { logger.setObservedTimestamp(it) }
    timestamp?.let { logger.setTimestamp(it) }
    logger.emit()
}

fun ElasticOtelAgent.span(
    name: String,
    attributes: Attributes? = null,
    kind: SpanKind? = null,
    parentContext: Context? = null,
    makeCurrent: Boolean = true,
    body: (Span) -> Unit
) {
    val builder = getOpenTelemetry().getTracer(SCOPE_NAME).spanBuilder(name)
    attributes?.let { builder.setAllAttributes(it) }
    kind?.let { builder.setSpanKind(it) }
    parentContext?.let { builder.setParent(it) }

    val span = builder.startSpan()
    val scope: Scope? = if (makeCurrent) span.makeCurrent() else null
    try {
        body(span)
    } catch (e: Throwable) {
        span.setStatus(StatusCode.ERROR)
        span.recordException(e)
    } finally {
        scope?.close()
        span.end()
    }
}