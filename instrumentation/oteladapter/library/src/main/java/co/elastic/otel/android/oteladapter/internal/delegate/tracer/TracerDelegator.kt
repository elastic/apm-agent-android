package co.elastic.otel.android.oteladapter.internal.delegate.tracer

import co.elastic.otel.android.oteladapter.internal.delegate.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tracer.span.NoopSpanBuilder
import co.elastic.otel.android.oteladapter.internal.delegate.tracer.span.SpanBuilderDelegator
import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentSet
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer

class TracerDelegator(initialValue: Tracer) : Delegator<Tracer>(initialValue), Tracer {
    private val spanBuilders =
        WeakConcurrentSet<SpanBuilderDelegator>(WeakConcurrentSet.Cleaner.INLINE)

    override fun spanBuilder(spanName: String): SpanBuilder? {
        spanBuilders.expungeStaleEntries()
        val builder = getDelegate().spanBuilder(spanName)
        if (builder != NoopSpanBuilder.INSTANCE) {
            spanBuilders.add(SpanBuilderDelegator(builder))
        }
        return builder
    }

    override fun reset() {
        super.reset()
        for (builder in spanBuilders) {
            builder?.reset()
        }
        spanBuilders.clear()
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