package co.elastic.otel.android.oteladapter.internal.delegate

import co.elastic.otel.android.oteladapter.internal.delegate.context.ContextPropagatorsDelegate
import co.elastic.otel.android.oteladapter.internal.delegate.logger.LoggerProviderDelegator
import co.elastic.otel.android.oteladapter.internal.delegate.meter.MeterProviderDelegator
import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tracer.TracerProviderDelegator
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.logs.LoggerProvider
import io.opentelemetry.api.metrics.MeterProvider
import io.opentelemetry.api.trace.TracerProvider
import io.opentelemetry.context.propagation.ContextPropagators

internal class OpenTelemetryDelegator(initialValue: OpenTelemetry) :
    Delegator<OpenTelemetry>(initialValue),
    OpenTelemetry {
    private val tracerProvider = TracerProviderDelegator(initialValue.tracerProvider)
    private val meterProvider = MeterProviderDelegator(initialValue.meterProvider)
    private val loggerProvider = LoggerProviderDelegator(initialValue.logsBridge)
    private val contextPropagators = ContextPropagatorsDelegate(initialValue.propagators)

    override fun setDelegate(value: OpenTelemetry) {
        super.setDelegate(value)
        tracerProvider.setDelegate(value.tracerProvider)
        meterProvider.setDelegate(value.meterProvider)
        loggerProvider.setDelegate(value.logsBridge)
        contextPropagators.setDelegate(value.propagators)
    }

    override fun reset() {
        super.reset()
        tracerProvider.reset()
        meterProvider.reset()
        loggerProvider.reset()
        contextPropagators.reset()
    }

    override fun getTracerProvider(): TracerProvider {
        return tracerProvider
    }

    override fun getMeterProvider(): MeterProvider {
        return meterProvider
    }

    override fun getLogsBridge(): LoggerProvider {
        return loggerProvider
    }

    override fun getPropagators(): ContextPropagators {
        return contextPropagators
    }

    override fun getNoopValue(): OpenTelemetry {
        return NOOP_INSTANCE
    }

    companion object {
        private val NOOP_INSTANCE = OpenTelemetry.noop()
    }
}