package co.elastic.otel.android.internal.services.network.query

import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

internal interface NetworkQueryManager {
    fun setChangeListener(listener: NetworkChangeListener)

    fun getNetworkType(): Int

    fun start()

    fun stop()
}