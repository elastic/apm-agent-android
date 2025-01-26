package co.elastic.otel.android.internal.services.network.query

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.telephony.TelephonyManager
import androidx.annotation.GuardedBy
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

internal class NetworkApi21QueryManager(
    private val connectivityManager: ConnectivityManager,
    private val telephonyManager: TelephonyManager
) : NetworkCallback(), NetworkQueryManager {
    internal lateinit var listener: NetworkChangeListener
    private val networkLock = Any()

    @GuardedBy("networkLock")
    private var currentNetwork: Network? = null

    override fun setChangeListener(listener: NetworkChangeListener) {
        this.listener = listener
    }

    override fun getNetworkType(): Int {
        return telephonyManager.networkType
    }

    override fun start() {
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(), this
        )
    }

    override fun stop() {
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        connectivityManager.getNetworkCapabilities(network)?.let { capabilities ->
            onNetworkUpdate(network, capabilities)
        }
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        onNetworkUpdate(network, networkCapabilities)
    }

    private fun onNetworkUpdate(network: Network, capabilities: NetworkCapabilities) {
        if (network == connectivityManager.activeNetwork) {
            onActiveNetworkSet(network, capabilities)
        }
    }

    override fun onLost(network: Network) {
        if (connectivityManager.activeNetwork == null) {
            onNetworkLost()
        }
        super.onLost(network)
    }

    private fun onActiveNetworkSet(network: Network, capabilities: NetworkCapabilities) {
        synchronized(networkLock) {
            if (network != currentNetwork) {
                currentNetwork = network
                listener.onNewNetwork(capabilities)
            }
        }
    }

    private fun onNetworkLost() {
        synchronized(networkLock) {
            currentNetwork = null
            listener.onNetworkLost()
        }
    }
}