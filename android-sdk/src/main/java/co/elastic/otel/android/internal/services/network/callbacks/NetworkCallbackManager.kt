package co.elastic.otel.android.internal.services.network.callbacks

import android.net.ConnectivityManager
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

internal interface NetworkCallbackManager {
    fun setListener(listener: NetworkChangeListener)

    fun register(connectivityManager: ConnectivityManager)

    fun unregister(connectivityManager: ConnectivityManager)
}