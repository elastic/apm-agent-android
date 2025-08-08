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
package co.elastic.otel.android.oteladapter.internal.delegate.tracer.span

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import java.util.concurrent.TimeUnit

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE")
class NoopSpanBuilder private constructor() : SpanBuilder {
    private var spanContext: SpanContext = Span.current().spanContext

    override fun startSpan(): Span? {
        return Span.wrap(spanContext)
    }

    override fun setParent(context: Context?): NoopSpanBuilder {
        if (context == null) {
            return this
        }
        spanContext = Span.fromContext(context).spanContext
        return this
    }

    override fun setNoParent(): NoopSpanBuilder {
        spanContext = SpanContext.getInvalid()
        return this
    }

    override fun addLink(spanContext: SpanContext): NoopSpanBuilder {
        return this
    }

    override fun addLink(spanContext: SpanContext, attributes: Attributes): NoopSpanBuilder {
        return this
    }

    override fun setAttribute(key: String, value: String): NoopSpanBuilder {
        return this
    }

    override fun setAttribute(key: String, value: Long): NoopSpanBuilder {
        return this
    }

    override fun setAttribute(key: String, value: Double): NoopSpanBuilder {
        return this
    }

    override fun setAttribute(key: String, value: Boolean): NoopSpanBuilder {
        return this
    }

    override fun <T> setAttribute(key: AttributeKey<T?>, value: T?): NoopSpanBuilder {
        return this
    }

    override fun setAllAttributes(attributes: Attributes): NoopSpanBuilder {
        return this
    }

    override fun setSpanKind(spanKind: SpanKind): NoopSpanBuilder {
        return this
    }

    override fun setStartTimestamp(startTimestamp: Long, unit: TimeUnit): NoopSpanBuilder {
        return this
    }

    companion object {
        val INSTANCE = NoopSpanBuilder()
    }
}
