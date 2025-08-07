package co.elastic.otel.android.oteladapter.internal.delegate.tracer.span

import co.elastic.otel.android.oteladapter.internal.delegate.Delegator
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import java.util.concurrent.TimeUnit

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