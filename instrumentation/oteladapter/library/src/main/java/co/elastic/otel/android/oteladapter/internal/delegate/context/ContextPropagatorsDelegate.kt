package co.elastic.otel.android.oteladapter.internal.delegate.context

import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator

internal class ContextPropagatorsDelegate(initialValue: ContextPropagators) :
    Delegator<ContextPropagators>(initialValue), ContextPropagators {
    private val textMapPropagator = TextMapPropagatorDelegate(initialValue.textMapPropagator)

    override fun setDelegate(value: ContextPropagators) {
        super.setDelegate(value)
        textMapPropagator.setDelegate(value.textMapPropagator)
    }

    override fun reset() {
        super.reset()
        textMapPropagator.reset()
    }

    override fun getTextMapPropagator(): TextMapPropagator {
        return textMapPropagator
    }

    override fun getNoopValue(): ContextPropagators {
        return NOOP_INSTANCE
    }

    companion object {
        private val NOOP_INSTANCE = ContextPropagators.noop()
    }
}