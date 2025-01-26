package co.elastic.otel.android.internal.services.network.query

import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

internal interface NetworkQueryManager {
    fun setChangeListener(listener: NetworkChangeListener)

    fun getNetworkType(telephonyManager: TelephonyManager): Int

    fun register(connectivityManager: ConnectivityManager)

    fun unregister(connectivityManager: ConnectivityManager)
}