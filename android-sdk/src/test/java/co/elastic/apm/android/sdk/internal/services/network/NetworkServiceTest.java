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
package co.elastic.apm.android.sdk.internal.services.network;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NetworkServiceTest {
    @Mock
    private ConnectivityManager connectivityManager;
    @Mock
    private TelephonyManager telephonyManager;
    private NetworkService networkService;

    @Before
    public void setUp() {
        networkService = new NetworkService(connectivityManager, telephonyManager);
    }

    @Test
    public void getCarrierInfo_whenSimOperatorIsNull() {
        doReturn(null).when(telephonyManager).getSimOperator();
        doReturn(TelephonyManager.SIM_STATE_READY).when(telephonyManager).getSimState();

        assertNull(networkService.getCarrierInfo());
    }

    @Test
    public void getCarrierInfo_whenSimOperatorIsEmpty() {
        doReturn("").when(telephonyManager).getSimOperator();
        doReturn(TelephonyManager.SIM_STATE_READY).when(telephonyManager).getSimState();

        assertNull(networkService.getCarrierInfo());
    }
}