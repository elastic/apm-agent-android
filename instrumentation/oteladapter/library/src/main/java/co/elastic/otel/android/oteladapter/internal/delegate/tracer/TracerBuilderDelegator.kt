package co.elastic.otel.android.oteladapter.internal.delegate.tracer

import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tools.MultipleReference
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.TracerBuilder

class TracerBuilderDelegator(
    initialValue: TracerBuilder,
    private val references: MultipleReference<Tracer>
) : Delegator<TracerBuilder>(initialValue), TracerBuilder {

    override fun setSchemaUrl(schemaUrl: String): TracerBuilder? {
        return getDelegate().setSchemaUrl(schemaUrl)
    }

    override fun setInstrumentationVersion(instrumentationScopeVersion: String): TracerBuilder? {
        return getDelegate().setInstrumentationVersion(instrumentationScopeVersion)
    }

    override fun build(): Tracer? {
        return references.maybeAdd(getDelegate().build())
    }

    override fun getNoopValue(): TracerBuilder {
        return NOOP_INSTANCE
    }

    companion object {
        val NOOP_INSTANCE: TracerBuilder = NoopTracerBuilder()
    }

    private class NoopTracerBuilder : TracerBuilder {
        override fun setSchemaUrl(schemaUrl: String): TracerBuilder? {
            return this
        }

        override fun setInstrumentationVersion(instrumentationScopeVersion: String): TracerBuilder? {
            return this
        }

        override fun build(): Tracer? {
            return TracerDelegator.NoopTracer.INSTANCE
        }
    }
}