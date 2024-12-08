/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.sdk.internal.services.kotlin.network

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.internal.services.kotlin.Service
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.internal.services.kotlin.appinfo.AppInfoService
import co.elastic.apm.android.sdk.internal.services.kotlin.network.data.CarrierInfo
import co.elastic.apm.android.sdk.internal.services.kotlin.network.data.NetworkType

class NetworkService internal constructor(
    serviceManager: ServiceManager,
    private val connectivityManager: ConnectivityManager,
    private val telephonyManager: TelephonyManager
) : NetworkCallback(), Service {
    private var type: NetworkType = NetworkType.None
    private val appInfoService: AppInfoService by lazy { serviceManager.getAppInfoService() }

    override fun start() {
        connectivityManager.registerDefaultNetworkCallback(this)
    }

    override fun stop() {
        connectivityManager.unregisterNetworkCallback(this)
    }

    fun getType(): NetworkType = synchronized(this) {
        return type
    }

    private fun setType(networkType: NetworkType) = synchronized(this) {
        this.type = networkType
    }

    fun getCarrierInfo(): CarrierInfo? {
        val simOperator = simOperator ?: return null

        val mcc = simOperator.substring(0, 3)
        val mnc = simOperator.substring(3)
        return CarrierInfo(
            telephonyManager.simOperatorName,
            mcc,
            mnc,
            telephonyManager.simCountryIso
        )
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        type = getNetworkType(networkCapabilities)
    }

    private fun getNetworkType(networkCapabilities: NetworkCapabilities): NetworkType {
        return if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            NetworkType.Cell(getSubtypeName())
        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            NetworkType.Wifi
        } else {
            NetworkType.Unknown
        }
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        type = NetworkType.None
    }

    private val simOperator: String?
        get() {
            if (telephonyManager.simState == TelephonyManager.SIM_STATE_READY) {
                val simOperator = telephonyManager.simOperator
                if (simOperator != null && simOperator.length > 3) {
                    return simOperator
                }
            }

            return null
        }

    @SuppressLint("MissingPermission")
    private fun getSubtypeName(): String? {
        if (!appInfoService.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
            Elog.getLogger().info("Not collecting cell network subtype due to permission missing")
            return null
        }

        return when (telephonyManager.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO_B"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "CDMA2000_1XRTT"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_IDEN -> "IDEN"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPAP"
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
            TelephonyManager.NETWORK_TYPE_NR -> "NR"
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> null
            else -> null
        }
    }

    companion object {
        fun create(application: Application, serviceManager: ServiceManager): NetworkService {
            val connectivityManager =
                application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val telephonyManager =
                application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return NetworkService(serviceManager, connectivityManager, telephonyManager)
        }
    }
}