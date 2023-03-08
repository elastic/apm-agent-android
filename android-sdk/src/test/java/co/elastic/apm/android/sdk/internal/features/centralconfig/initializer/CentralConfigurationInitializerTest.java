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
import co.elastic.apm.android.sdk.internal.utilities.concurrency.Result;
import co.elastic.apm.android.sdk.testutils.ImmediateBackgroundExecutor;

public class CentralConfigurationInitializerTest {
    private CentralConfigurationManager manager;
    private CentralConfigurationInitializer initializer;
    private ConfigurationPollManager pollManager;

    @Before
    public void setUp() {
        manager = mock(CentralConfigurationManager.class);
        pollManager = mock(ConfigurationPollManager.class);
        initializer = new CentralConfigurationInitializer(new ImmediateBackgroundExecutor(), manager, pollManager);
    }

    @Test
    public void verifyInitialization() throws IOException {
        initializer.initialize();

        InOrder inOrder = inOrder(manager);
        inOrder.verify(manager).publishCachedConfig();
        inOrder.verify(manager).sync();
    }

    @Test
    public void whenFirstFetchSucceeds_schedulePollsBasedOnReceivedMaxAge() {
        Integer maxAgeReceived = 14;
        initializer.onFinish(Result.success(maxAgeReceived));

        verify(pollManager).scheduleInSeconds(14);
        verifyNoMoreInteractions(pollManager);
    }

    @Test
    public void whenFirstFetchSucceeds_withNoMaxAgeProvided_scheduleNextPollOnDefaultDelay() {
        initializer.onFinish(Result.success(null));

        verify(pollManager).scheduleDefault();
        verifyNoMoreInteractions(pollManager);
    }

    @Test
    public void whenFirstFetchFailed_scheduleNextPollOnDefaultDelay() {
        initializer.onFinish(Result.error(new Exception()));

        verify(pollManager).scheduleDefault();
        verifyNoMoreInteractions(pollManager);
    }
}