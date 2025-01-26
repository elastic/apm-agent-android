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

import android.annotation.TargetApi
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener

@TargetApi(Build.VERSION_CODES.N)
internal class NetworkApi24QueryManager(
    private val connectivityManager: ConnectivityManager,
    private val telephonyManager: TelephonyManager
) : NetworkCallback(), NetworkQueryManager {
    internal lateinit var listener: NetworkChangeListener

    override fun setChangeListener(listener: NetworkChangeListener) {
        this.listener = listener
    }

    override fun getNetworkType(): Int {
        return telephonyManager.dataNetworkType
    }

    override fun start() {
        connectivityManager.registerDefaultNetworkCallback(this)
    }

    override fun stop() {
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