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
package co.elastic.apm.android.sdk.internal.time.ntp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import co.elastic.apm.android.sdk.testutils.BaseTest;

@RunWith(MockitoJUnitRunner.class)
public class NtpManagerTest extends BaseTest {

    @Mock
    public TrueTimeWrapper trueTimeWrapper;

    private NtpManager ntpManager;

    @Before
    public void setUp() {
        ntpManager = new NtpManager(trueTimeWrapper);
    }

    @Test
    public void verifyInitializationParameters() {
        ntpManager.initialize();

        verify(trueTimeWrapper).withSharedPreferencesCache();
        verify(trueTimeWrapper).withRootDispersionMax(200);
        verify(trueTimeWrapper).withRootDelayMax(200);
    }

    @Test
    public void whenInitializationSucceeds_setManagerInitialized() throws IOException {
        assertFalse(ntpManager.isInitialized());

        ntpManager.onPeriodicTaskRun();

        verify(trueTimeWrapper).initialize();
        assertTrue(ntpManager.isInitialized());
        assertTrue(ntpManager.isFinished());
    }

    @Test
    public void whenInitializationFails_doNotSetManagerInitialized() throws IOException {
        doThrow(new IOException()).when(trueTimeWrapper).initialize();

        ntpManager.onPeriodicTaskRun();

        assertFalse(ntpManager.isInitialized());
        assertFalse(ntpManager.isFinished());
    }

    @Test
    public void whenAlreadyInitialized_avoidReInitializing() throws IOException {
        doReturn(true).when(trueTimeWrapper).isInitialized();

        ntpManager.initialize();
        ntpManager.onPeriodicTaskRun();

        verify(trueTimeWrapper, never()).initialize();
    }
}