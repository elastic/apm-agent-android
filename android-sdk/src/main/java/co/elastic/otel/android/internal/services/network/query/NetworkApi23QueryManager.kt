package co.elastic.otel.android.internal.services.network.query

import android.annotation.TargetApi
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.GuardedBy
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

@TargetApi(Build.VERSION_CODES.M)
internal class NetworkApi23QueryManager(
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
        onNetworkUpdate(network)
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        onNetworkUpdate(network)
    }

    private fun onNetworkUpdate(network: Network) {
        if (network == connectivityManager.activeNetwork) {
            onActiveNetworkSet(network)
        }
    }

    override fun onLost(network: Network) {
        if (connectivityManager.activeNetwork == null) {
            onNetworkLost()
        }
        super.onLost(network)
    }

    private fun onActiveNetworkSet(network: Network) {
        synchronized(networkLock) {
            if (network != currentNetwork) {
                currentNetwork = network
                listener.onNewNetwork()
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