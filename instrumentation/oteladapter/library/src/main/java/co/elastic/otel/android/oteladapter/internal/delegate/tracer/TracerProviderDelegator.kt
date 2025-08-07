package co.elastic.otel.android.oteladapter.internal.delegate.tracer

import co.elastic.otel.android.oteladapter.internal.delegate.Delegator
import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentSet
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.TracerBuilder
import io.opentelemetry.api.trace.TracerProvider

class TracerProviderDelegator(initialValue: TracerProvider) :
    Delegator<TracerProvider>(initialValue), TracerProvider {
    private val tracers =
        WeakConcurrentSet<TracerDelegator>(WeakConcurrentSet.Cleaner.INLINE)

    override fun get(instrumentationScopeName: String): Tracer? {
        tracers.expungeStaleEntries()
        val tracer = getDelegate().get(instrumentationScopeName)
        maybeStore(tracer)
        return tracer
    }

    override fun get(
        instrumentationScopeName: String,
        instrumentationScopeVersion: String
    ): Tracer? {
        tracers.expungeStaleEntries()
        val tracer = getDelegate().get(instrumentationScopeName, instrumentationScopeVersion)
        maybeStore(tracer)
        return tracer
    }

    override fun tracerBuilder(instrumentationScopeName: String): TracerBuilder? {
        return getDelegate().tracerBuilder(instrumentationScopeName)
    }

    private fun maybeStore(tracer: Tracer) {
        if (tracer != TracerDelegator.NoopTracer.INSTANCE) {
            tracers.add(TracerDelegator(tracer))
        }
    }

    override fun getNoopValue(): TracerProvider {
        return NOOP_INSTANCE
    }

    override fun reset() {
        super.reset()
        for (delegator in tracers) {
            delegator?.reset()
        }
        tracers.clear()
    }

    companion object {
        private val NOOP_INSTANCE = NoopTracerProvider()
    }

    private class NoopTracerProvider : TracerProvider {
        override fun get(instrumentationScopeName: String): Tracer? {
            return TracerDelegator.NoopTracer.INSTANCE
        }

        override fun get(
            instrumentationScopeName: String,
            instrumentationScopeVersion: String
        ): Tracer? {
            return TracerDelegator.NoopTracer.INSTANCE
        }
    }
}