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
package co.elastic.apm.android.sdk.internal.services.network

import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import co.elastic.apm.android.sdk.internal.services.kotlin.appinfo.AppInfoService
import co.elastic.apm.android.sdk.internal.services.kotlin.network.NetworkService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NetworkServiceTest {
    @MockK
    private lateinit var connectivityManager: ConnectivityManager

    @MockK
    private lateinit var telephonyManager: TelephonyManager

    @MockK
    private lateinit var appInfoService: AppInfoService
    private lateinit var networkService: NetworkService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        networkService = NetworkService(appInfoService, connectivityManager, telephonyManager)
    }

    @Test
    fun `Get carrier info when sim operator is null`() {
        every { telephonyManager.simOperator }.returns(null)
        every { telephonyManager.simState }.returns(TelephonyManager.SIM_STATE_READY)
        assertNull(networkService.getCarrierInfo())
    }

    @Test
    fun `Get carrier info when sim operator is empty`() {
        every { telephonyManager.simOperator }.returns("")
        every { telephonyManager.simState }.returns(TelephonyManager.SIM_STATE_READY)
        assertNull(networkService.getCarrierInfo())
    }

    @Test
    fun `Get carrier info from first sim operator response`() {
        every { telephonyManager.simOperatorName }.returns("")
        every { telephonyManager.simCountryIso }.returns("")
        every { telephonyManager.simOperator }.returns("1234").andThen("")
        every { telephonyManager.simState }.returns(TelephonyManager.SIM_STATE_READY)

        assertNotNull(networkService.getCarrierInfo())
    }
}