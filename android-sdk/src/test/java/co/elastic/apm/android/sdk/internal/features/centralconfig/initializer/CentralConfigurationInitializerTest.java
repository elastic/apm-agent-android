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
package co.elastic.apm.android.sdk.internal.features.centralconfig.initializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;

import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;

public class CentralConfigurationInitializerTest {
    private CentralConfigurationManager manager;
    private CentralConfigurationInitializer initializer;
    private ConfigurationPollManager pollManager;
    private PeriodicWorkService periodicWorkService;

    @Before
    public void setUp() {
        manager = mock(CentralConfigurationManager.class);
        pollManager = mock(ConfigurationPollManager.class);
        periodicWorkService = mock(PeriodicWorkService.class);
        initializer = new CentralConfigurationInitializer(manager, pollManager, periodicWorkService);
    }

    @Test
    public void verifyInitialization() throws IOException {
        initializer.onPeriodicTaskRun();

        InOrder inOrder = inOrder(manager);
        inOrder.verify(manager).publishCachedConfig();
        inOrder.verify(manager).sync();
        assertEquals(0, initializer.getMillisToWaitBeforeNextRun());
        assertTrue(initializer.isFinished());
    }

    @Test
    public void whenFirstFetchSucceeds_schedulePollsBasedOnReceivedMaxAge() throws IOException {
        Integer maxAgeReceived = 14;
        doReturn(maxAgeReceived).when(manager).sync();

        initializer.onPeriodicTaskRun();

        verify(pollManager).scheduleInSeconds(14);
        verify(periodicWorkService).addTask(pollManager);
        verifyNoMoreInteractions(pollManager);
    }

    @Test
    public void whenFirstFetchSucceeds_withNoMaxAgeProvided_scheduleNextPollOnDefaultDelay() throws IOException {
        doReturn(null).when(manager).sync();

        initializer.onPeriodicTaskRun();

        verify(pollManager).scheduleDefault();
        verify(periodicWorkService).addTask(pollManager);
        verifyNoMoreInteractions(pollManager);
    }

    @Test
    public void whenFirstFetchFailed_scheduleNextPollOnDefaultDelay() throws IOException {
        doThrow(new IOException()).when(manager).sync();

        initializer.onPeriodicTaskRun();

        verify(pollManager).scheduleDefault();
        verify(periodicWorkService).addTask(pollManager);
        verifyNoMoreInteractions(pollManager);
    }
}