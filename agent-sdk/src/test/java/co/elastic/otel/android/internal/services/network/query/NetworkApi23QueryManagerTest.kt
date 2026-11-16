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

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import co.elastic.otel.android.internal.services.network.listener.NetworkChangeListener
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkApi23QueryManagerTest {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var listener: NetworkChangeListener
    private lateinit var manager: NetworkApi23QueryManager

    @Before
    fun setUp() {
        connectivityManager = mockk()
        telephonyManager = mockk()
        listener = mockk(relaxed = true)
        manager = NetworkApi23QueryManager(connectivityManager, telephonyManager)
        manager.setChangeListener(listener)
    }

    @Test
    fun `Network type comes from the legacy telephony API`() {
        every { telephonyManager.networkType } returns TelephonyManager.NETWORK_TYPE_EDGE

        assertEquals(TelephonyManager.NETWORK_TYPE_EDGE, manager.getNetworkType())
        verify(exactly = 0) { telephonyManager.dataNetworkType }
    }

    @Test
    fun `Only the active network reports a capability change`() {
        val activeNetwork = mockk<Network>()
        val otherNetwork = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities>()
        every { connectivityManager.activeNetwork } returns activeNetwork

        manager.onCapabilitiesChanged(otherNetwork, capabilities)
        verify(exactly = 0) { listener.onNewNetwork(any()) }

        manager.onCapabilitiesChanged(activeNetwork, capabilities)
        verify(exactly = 1) { listener.onNewNetwork(capabilities) }

        manager.onCapabilitiesChanged(activeNetwork, capabilities)
        verify(exactly = 1) { listener.onNewNetwork(capabilities) }
    }
}
