package co.elastic.otel.android.internal.services.network.query

import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

internal interface NetworkQueryManager {
    companion object {
        fun create(
            connectivityManager: ConnectivityManager,
            telephonyManager: TelephonyManager
        ): NetworkQueryManager {
            val currentApi = Build.VERSION.SDK_INT
            return when {
                currentApi == Build.VERSION_CODES.M -> NetworkApi23QueryManager(
                    connectivityManager,
                    telephonyManager
                )

                currentApi < Build.VERSION_CODES.M -> NetworkApi21QueryManager(
                    connectivityManager,
                    telephonyManager
                )

                else -> NetworkApi24QueryManager(connectivityManager, telephonyManager)
            }
        }
    }

    fun setChangeListener(listener: NetworkChangeListener)

    fun getNetworkType(): Int

    fun start()

    fun stop()
}