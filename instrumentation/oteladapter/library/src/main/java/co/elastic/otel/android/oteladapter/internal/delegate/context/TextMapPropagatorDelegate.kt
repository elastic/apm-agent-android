package co.elastic.otel.android.oteladapter.internal.delegate.context

import co.elastic.otel.android.oteladapter.internal.delegate.Delegator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.context.propagation.TextMapSetter

internal class TextMapPropagatorDelegate(initialValue: TextMapPropagator) :
    Delegator<TextMapPropagator>(initialValue), TextMapPropagator {

    override fun fields(): Collection<String?>? {
        return getDelegate().fields()
    }

    override fun <C : Any?> inject(
        context: Context,
        carrier: C?,
        setter: TextMapSetter<C?>
    ) {
        getDelegate().inject(context, carrier, setter)
    }

    override fun <C : Any?> extract(
        context: Context,
        carrier: C?,
        getter: TextMapGetter<C?>
    ): Context? {
        return getDelegate().extract(context, carrier, getter)
    }

    override fun getNoopValue(): TextMapPropagator {
        return NOOP_INSTANCE
    }

    companion object {
        private val NOOP_INSTANCE = TextMapPropagator.noop()
    }
}