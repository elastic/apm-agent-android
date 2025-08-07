package co.elastic.otel.android.oteladapter.internal.delegate.tracer

import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tools.MultipleReference
import co.elastic.otel.android.oteladapter.internal.delegate.tracer.span.NoopSpanBuilder
import co.elastic.otel.android.oteladapter.internal.delegate.tracer.span.SpanBuilderDelegator
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer

class TracerDelegator(initialValue: Tracer) : Delegator<Tracer>(initialValue), Tracer {
    private val spanBuilders = MultipleReference<SpanBuilder>(NoopSpanBuilder.INSTANCE) {
        SpanBuilderDelegator(it)
    }

    override fun spanBuilder(spanName: String): SpanBuilder? {
        return spanBuilders.maybeAdd(getDelegate().spanBuilder(spanName))
    }

    override fun reset() {
        super.reset()
        spanBuilders.reset()
    }

    override fun getNoopValue(): Tracer {
        return NoopTracer.INSTANCE
    }

    class NoopTracer private constructor() : Tracer {

        override fun spanBuilder(spanName: String): SpanBuilder? {
            return NoopSpanBuilder.INSTANCE
        }

        companion object {
            val INSTANCE = NoopTracer()
        }
    }
}