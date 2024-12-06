package co.elastic.apm.android.sdk.tools.provider

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

class MutableProvider<T>(initialValue: T) : Provider<T> {
    private val value = AtomicReference(initialValue)
    private val listeners = CopyOnWriteArrayList<Listener<T>>()
    private val setLock = Any()

    override fun get(): T {
        return value.get()
    }

    fun set(value: T) = synchronized(setLock) {
        if (this.value.get() != value) {
            this.value.set(value)
            notifyChange()
        }
    }

    private fun notifyChange() {
        listeners.forEach {
            it.onChange(this)
        }
    }

    fun addListener(listener: Listener<T>) {
        listeners.addIfAbsent(listener)
    }

    interface Listener<T> {
        fun onChange(provider: Provider<T>)
    }
}