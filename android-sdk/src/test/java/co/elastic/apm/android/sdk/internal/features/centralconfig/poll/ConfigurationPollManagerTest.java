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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;

public class ConfigurationPollManagerTest {
    private CentralConfigurationManager manager;
    private ConfigurationPollManager pollManager;
    private SystemTimeProvider timeProvider;
    private PeriodicWorkService periodicWorkService;
    private static final long INITIAL_CURRENT_TIME_MILLIS = 1000;

    @Before
    public void setUp() {
        manager = mock(CentralConfigurationManager.class);
        timeProvider = mock(SystemTimeProvider.class);
        doReturn(INITIAL_CURRENT_TIME_MILLIS).when(timeProvider).getCurrentTimeMillis();
        periodicWorkService = mock(PeriodicWorkService.class);
        pollManager = ConfigurationPollManager.create(manager, periodicWorkService, timeProvider);
    }

    @Test
    public void verifyInitialization() {
        verify(periodicWorkService).addTask(pollManager);
    }

    @Test
    public void whenScheduledPollSucceeds_rescheduleBasedOnResponse() throws IOException {
        Integer maxAgeReturned = 15;
        doReturn(maxAgeReturned).when(manager).sync();

        assertTrue(pollManager.runPeriodicTask());

        verify(manager).sync();
        verifyNextPollIsScheduledAfterSeconds(15);
    }

    @Test
    public void whenScheduledPollSucceeds_withNoMaxAgeResponse_rescheduleWithDefaultDelay() throws IOException {
        doReturn(null).when(manager).sync();

        assertTrue(pollManager.runPeriodicTask());

        verify(manager).sync();
        verifyNextPollIsScheduledAfterSeconds(60);
    }

    @Test
    public void whenScheduledPollFails_rescheduleWithDefaultDelay() throws IOException {
        doThrow(IOException.class).when(manager).sync();

        assertTrue(pollManager.runPeriodicTask());

        verifyNextPollIsScheduledAfterSeconds(60);
    }

    @Test
    public void whenNextScheduleIsNotDue_doNotRun() throws IOException {
        pollManager.scheduleInSeconds(10);

        assertTrue(pollManager.runPeriodicTask());

        // It shouldn't have run.
        verify(manager, never()).sync();

        // Fast forward time to just before it's due and run again.
        fastForwardCurrentTimeInSeconds(9);
        assertTrue(pollManager.runPeriodicTask());

        // It shouldn't have run yet.
        verify(manager, never()).sync();

        // Fast forward time to just in time for the next run and run again.
        fastForwardCurrentTimeInSeconds(1);
        assertTrue(pollManager.runPeriodicTask());

        //  Now it should have run.
        verify(manager).sync();
    }

    @Test
    public void verifyScheduleDefault() {
        pollManager.scheduleDefault();

        verifyNextPollIsScheduledAfterSeconds(60);
    }

    private void fastForwardCurrentTimeInSeconds(long seconds) {
        doReturn(timeProvider.getCurrentTimeMillis() + (seconds * 1000)).when(timeProvider).getCurrentTimeMillis();
    }

    private void verifyNextPollIsScheduledAfterSeconds(long seconds) {
        assertEquals((seconds * 1000) + timeProvider.getCurrentTimeMillis(), pollManager.nextExecutionTime);
    }
}