package co.elastic.otel.android.oteladapter.internal.delegate.meter

import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import io.opentelemetry.api.metrics.MeterBuilder
import io.opentelemetry.api.metrics.MeterProvider

class MeterProviderDelegator(initialValue: MeterProvider) : Delegator<MeterProvider>(initialValue),
    MeterProvider {

    override fun meterBuilder(instrumentationScopeName: String): MeterBuilder? {
        return getDelegate().meterBuilder(instrumentationScopeName)
    }

    override fun getNoopValue(): MeterProvider {
        return NOOP_INSTANCE
    }

    companion object {
        val NOOP_INSTANCE: MeterProvider = MeterProvider.noop()
    }
}