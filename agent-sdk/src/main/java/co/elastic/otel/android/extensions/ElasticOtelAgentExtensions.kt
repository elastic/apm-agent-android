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

private const val DEFAULT_SCOPE_NAME = "co.elastic.otel.android.extensions"

/**
 * Convenience method to send OpenTelemetry's [Log Records](https://opentelemetry.io/docs/concepts/signals/logs/).
 *
 * @param body The log body/text.
 * @param severity The log's severity. Defaults to OpenTelemetry SDK's default.
 * @param severityText The log's severity text. Defaults to OpenTelemetry SDK's default.
 * @param attributes The log's attributes. Defaults to OpenTelemetry SDK's default.
 * @param context The log's context. Defaults to OpenTelemetry SDK's default.
 * @param observedTimestamp The log's observed timestamp. Defaults to OpenTelemetry SDK's default.
 * @param timestamp The log's timestamp. Defaults to OpenTelemetry SDK's default.
 * @param scopeName The log's instrumentation scope name. Defaults to [DEFAULT_SCOPE_NAME].
 */
fun ElasticOtelAgent.log(
    body: String,
    severity: Severity? = null,
    severityText: String? = null,
    attributes: Attributes? = null,
    context: Context? = null,
    observedTimestamp: Instant? = null,
    timestamp: Instant? = null,
    scopeName: String = DEFAULT_SCOPE_NAME
) {
    val logger = getOpenTelemetry().logsBridge.get(scopeName).logRecordBuilder()
        .setBody(body)
    severity?.let { logger.setSeverity(it) }
    severityText?.let { logger.setSeverityText(it) }
    attributes?.let { logger.setAllAttributes(it) }
    context?.let { logger.setContext(it) }
    observedTimestamp?.let { logger.setObservedTimestamp(it) }
    timestamp?.let { logger.setTimestamp(it) }
    logger.emit()
}

/**
 * Convenience method to send OpenTelemetry's [Spans](https://opentelemetry.io/docs/concepts/signals/traces/#spans).
 *
 * @param name The span name.
 * @param attributes The span's attributes. Defaults to OpenTelemetry SDK's default.
 * @param kind The span's kind. Defaults to OpenTelemetry SDK's default.
 * @param parentContext The span's parent context. Defaults to OpenTelemetry SDK's default.
 * @param makeCurrent Whether the span will be automatically set as the "current one" within the thread it's created in, until its body's finished. Defaults to TRUE.
 * @param scopeName The span's instrumentation scope name. Defaults to [DEFAULT_SCOPE_NAME].
 * @param body The span's body. The span will start right before executing its body and will end right after its body's finished or an uncaught exception happens.
 */
fun ElasticOtelAgent.span(
    name: String,
    attributes: Attributes? = null,
    kind: SpanKind? = null,
    parentContext: Context? = null,
    makeCurrent: Boolean = true,
    scopeName: String = DEFAULT_SCOPE_NAME,
    body: (Span) -> Unit
) {
    val builder = getOpenTelemetry().getTracer(scopeName).spanBuilder(name)
    attributes?.let { builder.setAllAttributes(it) }
    kind?.let { builder.setSpanKind(it) }
    parentContext?.let { builder.setParent(it) }

    val span = builder.startSpan()
    val scope: Scope? = if (makeCurrent) span.makeCurrent() else null
    try {
        body(span)
    } catch (t: Throwable) {
        span.setStatus(StatusCode.ERROR)
        throw t
    } finally {
        scope?.close()
        span.end()
    }
}