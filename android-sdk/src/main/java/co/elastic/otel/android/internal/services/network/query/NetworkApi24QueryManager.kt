package co.elastic.otel.android.internal.services.network.query

import android.annotation.TargetApi
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

@TargetApi(Build.VERSION_CODES.N)
internal class NetworkApi24QueryManager : NetworkCallback(), NetworkQueryManager {
    internal lateinit var listener: NetworkChangeListener

    override fun setChangeListener(listener: NetworkChangeListener) {
        this.listener = listener
    }

    override fun getNetworkType(telephonyManager: TelephonyManager): Int {
        return telephonyManager.dataNetworkType
    }

    override fun register(connectivityManager: ConnectivityManager) {
        connectivityManager.registerDefaultNetworkCallback(this)
    }

    override fun unregister(connectivityManager: ConnectivityManager) {
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        listener.onNewNetwork(networkCapabilities)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        listener.onNetworkLost()
    }
}