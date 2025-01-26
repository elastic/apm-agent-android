package co.elastic.otel.android.internal.services.network.listener

import android.net.NetworkCapabilities

internal interface NetworkChangeListener {
    fun onNewNetwork(capabilities: NetworkCapabilities)

    fun onNetworkLost()
}