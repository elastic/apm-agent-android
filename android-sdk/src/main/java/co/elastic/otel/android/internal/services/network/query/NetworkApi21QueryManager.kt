package co.elastic.otel.android.internal.services.network.query

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

internal class NetworkApi21QueryManager : NetworkCallback(), NetworkQueryManager {
    internal lateinit var listener: NetworkChangeListener

    override fun setChangeListener(listener: NetworkChangeListener) {
        this.listener = listener
    }

    override fun getNetworkType(telephonyManager: TelephonyManager): Int {
        return telephonyManager.networkType
    }

    override fun register(connectivityManager: ConnectivityManager) {
        connectivityManager.registerNetworkCallback()
    }

    override fun unregister(connectivityManager: ConnectivityManager) {
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
    }
}