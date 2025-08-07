package co.elastic.otel.android.oteladapter.internal.delegate

import java.util.concurrent.atomic.AtomicReference

abstract class Delegator<T>(initialValue: T) {
    private val delegate = AtomicReference(initialValue)

    abstract fun getNoopValue(): T

    open fun setDelegate(value: T) {
        delegate.set(value)
    }

    open fun reset() {
        delegate.set(getNoopValue())
    }

    fun getDelegate(): T {
        return delegate.get()
    }
}