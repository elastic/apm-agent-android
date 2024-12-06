package co.elastic.apm.android.sdk.connectivity

import co.elastic.apm.android.sdk.tools.provider.Provider
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

open class ConnectivityConfigurationManager(initialValue: ConnectivityConfiguration) :
    Provider<ConnectivityConfiguration> {
    private val value = AtomicReference(initialValue)
    private val listeners = CopyOnWriteArrayList<Listener>()
    private val setLock = Any()

    override fun get(): ConnectivityConfiguration {
        return value.get()
    }

    fun set(value: ConnectivityConfiguration) = synchronized(setLock) {
        if (this.value.get() != value) {
            this.value.set(value)
            notifyChange()
        }
    }

    private fun notifyChange() {
        listeners.forEach {
            it.onConnectivityConfigurationChange()
        }
    }

    fun addListener(listener: Listener) {
        listeners.addIfAbsent(listener)
    }

    interface Listener {
        fun onConnectivityConfigurationChange()
    }
}