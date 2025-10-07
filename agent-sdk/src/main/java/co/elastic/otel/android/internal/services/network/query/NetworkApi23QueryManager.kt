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
package co.elastic.otel.android.internal.services.network.query

import android.Manifest
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.GuardedBy
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@RequiresApi(Build.VERSION_CODES.M)
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

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
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