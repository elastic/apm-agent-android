package co.elastic.otel.android.oteladapter.internal.delegate.tracer

import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tools.MultipleReference
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.TracerBuilder
import io.opentelemetry.api.trace.TracerProvider

class TracerProviderDelegator(initialValue: TracerProvider) :
    Delegator<TracerProvider>(initialValue), TracerProvider {
    private val tracerReferences = MultipleReference<Tracer>(TracerDelegator.NoopTracer.INSTANCE) {
        TracerDelegator(it)
    }
    private val tracerBuilderReferences = MultipleReference(TracerBuilderDelegator.NOOP_INSTANCE) {
        TracerBuilderDelegator(it, tracerReferences)
    }

    override fun get(instrumentationScopeName: String): Tracer? {
        return tracerReferences.maybeAdd(getDelegate().get(instrumentationScopeName))
    }

    override fun get(
        instrumentationScopeName: String,
        instrumentationScopeVersion: String
    ): Tracer? {
        return tracerReferences.maybeAdd(
            getDelegate().get(
                instrumentationScopeName,
                instrumentationScopeVersion
            )
        )
    }

    override fun tracerBuilder(instrumentationScopeName: String): TracerBuilder? {
        return tracerBuilderReferences.maybeAdd(getDelegate().tracerBuilder(instrumentationScopeName))
    }

    override fun getNoopValue(): TracerProvider {
        return NOOP_INSTANCE
    }

    override fun reset() {
        super.reset()
        tracerReferences.reset()
        tracerBuilderReferences.reset()
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