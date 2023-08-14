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
package co.elastic.apm.android.sdk.features.persistence.scheduler.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import co.elastic.apm.android.sdk.features.persistence.SignalDiskExporter;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;

public class DefaultExportSchedulerTest {
    private PeriodicWorkService service;
    private SignalDiskExporter signalDiskExporter;
    private DefaultExportScheduler exportScheduler;
    private PreferencesService preferencesService;
    private SystemTimeProvider timeProvider;
    private static final long DELAY_TIME_IN_MILLIS = 500;
    private static final long INITIAL_CURRENT_TIME = 1000;
    private static final String LAST_TIME_RUN_KEY = "last_time_exported_from_disk";

    @Before
    public void setUp() {
        service = mock(PeriodicWorkService.class);
        signalDiskExporter = mock(SignalDiskExporter.class);
        preferencesService = mock(PreferencesService.class);
        timeProvider = mock(SystemTimeProvider.class);
        doReturn(INITIAL_CURRENT_TIME).when(timeProvider).getCurrentTimeMillis();
        SignalDiskExporter.set(signalDiskExporter);
        exportScheduler = new DefaultExportScheduler(service, preferencesService, timeProvider, DELAY_TIME_IN_MILLIS);
    }

    @After
    public void tearDown() {
        SignalDiskExporter.resetForTesting();
    }

    @Test
    public void whenPersistenceIsEnabled_addSchedulerToPeriodicTasks_andKeepTaskEnabled() {
        exportScheduler.onPersistenceEnabled();

        verify(service).addTask(exportScheduler);
        assertFalse(exportScheduler.isTaskFinished());
        assertTrue(exportScheduler.shouldRunTask());
    }

    @Test
    public void whenPersistenceIsEnabled_getLastTimeRunFromPersistence() {
        long storedLastTimeRun = 900;
        doReturn(storedLastTimeRun).when(preferencesService).retrieveLong(LAST_TIME_RUN_KEY, 0);
        exportScheduler.onPersistenceEnabled();

        verify(preferencesService).retrieveLong(LAST_TIME_RUN_KEY, 0);

        assertFalse(exportScheduler.shouldRunTask());

        // Fast forward to just 1 millisecond before it's due.
        fastForwardTimeBy((storedLastTimeRun - DELAY_TIME_IN_MILLIS) - 1);
        assertFalse(exportScheduler.shouldRunTask());

        // Add the millisecond needed to run again.
        fastForwardTimeBy(1);
        assertTrue(exportScheduler.shouldRunTask());
    }

    @Test
    public void whenPersistenceIsDisabled_disableTask() {
        exportScheduler.onPersistenceDisabled();

        assertFalse(exportScheduler.shouldRunTask());
        assertTrue(exportScheduler.isTaskFinished());
    }

    @Test
    public void whenRunning_exportAllSignals() throws IOException {
        when(signalDiskExporter.exportBatchOfEach()).thenReturn(true).thenReturn(true).thenReturn(false);

        exportScheduler.runTask();

        verify(signalDiskExporter, times(3)).exportBatchOfEach();
    }

    @Test
    public void whenPersistenceIsReEnabled_reEnableTheScheduler() {
        exportScheduler.onPersistenceEnabled();
        exportScheduler.onPersistenceDisabled();
        clearInvocations(service);

        exportScheduler.onPersistenceEnabled();

        verify(service).addTask(exportScheduler);
        assertFalse(exportScheduler.isTaskFinished());
        assertTrue(exportScheduler.shouldRunTask());
    }

    @Test
    public void whenRunningTask_persistLastTimeItWasRun() {
        exportScheduler.runTask();

        verify(preferencesService).store(LAST_TIME_RUN_KEY, INITIAL_CURRENT_TIME);
    }

    @Test
    public void verifyTaskRunWaitingMinimumDelay() {
        assertTrue(exportScheduler.shouldRunTask());
        exportScheduler.runTask();

        assertFalse(exportScheduler.shouldRunTask());

        // Run again after the delay has passed:
        fastForwardTimeBy(DELAY_TIME_IN_MILLIS);

        assertTrue(exportScheduler.shouldRunTask());
    }

    private void fastForwardTimeBy(long milliseconds) {
        doReturn(milliseconds + timeProvider.getCurrentTimeMillis()).when(timeProvider).getCurrentTimeMillis();
    }
}