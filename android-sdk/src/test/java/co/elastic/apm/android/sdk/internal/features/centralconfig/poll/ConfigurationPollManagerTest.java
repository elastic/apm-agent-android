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
package co.elastic.apm.android.sdk.internal.features.centralconfig.poll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;

public class ConfigurationPollManagerTest {
    private CentralConfigurationManager manager;
    private ConfigurationPollManager pollManager;

    @Before
    public void setUp() {
        manager = mock(CentralConfigurationManager.class);
        pollManager = new ConfigurationPollManager(manager);
    }

    @Test
    public void whenScheduledPollSucceeds_rescheduleBasedOnResponse() throws IOException {
        Integer maxAgeReturned = 15;
        doReturn(maxAgeReturned).when(manager).sync();

        pollManager.onPeriodicTaskRun();

        verify(manager).sync();
        assertFalse(pollManager.isFinished());
        verifyNextPollIsScheduledAfterSeconds(15);
    }

    @Test
    public void whenScheduledPollSucceeds_withNoMaxAgeResponse_rescheduleWithDefaultDelay() throws IOException {
        doReturn(null).when(manager).sync();

        pollManager.onPeriodicTaskRun();

        verify(manager).sync();
        assertFalse(pollManager.isFinished());
        verifyNextPollIsScheduledAfterSeconds(60);
    }

    @Test
    public void whenScheduledPollFails_rescheduleWithDefaultDelay() throws IOException {
        doThrow(IOException.class).when(manager).sync();

        pollManager.onPeriodicTaskRun();

        assertFalse(pollManager.isFinished());
        verifyNextPollIsScheduledAfterSeconds(60);
    }

    @Test
    public void verifyScheduleDefault() {
        pollManager.scheduleDefault();

        verifyNextPollIsScheduledAfterSeconds(60);
    }

    private void verifyNextPollIsScheduledAfterSeconds(long seconds) {
        assertEquals(seconds * 1000, pollManager.getMillisToWaitBeforeNextRun());
    }
}