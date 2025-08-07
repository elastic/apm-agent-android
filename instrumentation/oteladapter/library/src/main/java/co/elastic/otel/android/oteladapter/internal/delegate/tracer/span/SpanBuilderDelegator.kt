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

import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
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
class SpanBuilderDelegator(initialValue: SpanBuilder) : Delegator<SpanBuilder>(initialValue),
    SpanBuilder {

    override fun setParent(context: Context): SpanBuilder? {
        return getDelegate().setParent(context)
    }

    override fun setNoParent(): SpanBuilder? {
        return getDelegate().setNoParent()
    }

    override fun addLink(spanContext: SpanContext): SpanBuilder? {
        return getDelegate().addLink(spanContext)
    }

    override fun addLink(
        spanContext: SpanContext,
        attributes: Attributes
    ): SpanBuilder? {
        return getDelegate().addLink(spanContext, attributes)
    }

    override fun setAttribute(
        key: String,
        value: String
    ): SpanBuilder? {
        return getDelegate().setAttribute(key, value)
    }

    override fun setAttribute(
        key: String,
        value: Long
    ): SpanBuilder? {
        return getDelegate().setAttribute(key, value)
    }

    override fun setAttribute(
        key: String,
        value: Double
    ): SpanBuilder? {
        return getDelegate().setAttribute(key, value)
    }

    override fun setAttribute(
        key: String,
        value: Boolean
    ): SpanBuilder? {
        return getDelegate().setAttribute(key, value)
    }

    override fun <T : Any?> setAttribute(
        key: AttributeKey<T?>,
        value: T & Any
    ): SpanBuilder? {
        return getDelegate().setAttribute(key, value)
    }

    override fun setSpanKind(spanKind: SpanKind): SpanBuilder? {
        return getDelegate().setSpanKind(spanKind)
    }

    override fun setStartTimestamp(
        startTimestamp: Long,
        unit: TimeUnit
    ): SpanBuilder? {
        return getDelegate().setStartTimestamp(startTimestamp, unit)
    }

    override fun startSpan(): Span? {
        return getDelegate().startSpan()
    }

    override fun getNoopValue(): SpanBuilder {
        return NoopSpanBuilder.INSTANCE
    }
}