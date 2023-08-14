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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import co.elastic.apm.android.sdk.features.persistence.SignalDiskExporter;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;

public class DefaultExportSchedulerTest {
    private PeriodicWorkService service;
    private SignalDiskExporter signalDiskExporter;
    private DefaultExportScheduler exportScheduler;
    private static final long DELAY_TIME_IN_MILLIS = 20_000;

    @Before
    public void setUp() {
        service = mock(PeriodicWorkService.class);
        signalDiskExporter = mock(SignalDiskExporter.class);
        SignalDiskExporter.set(signalDiskExporter);
        exportScheduler = new DefaultExportScheduler(service, DELAY_TIME_IN_MILLIS);
    }

    @After
    public void tearDown() {
        SignalDiskExporter.resetForTesting();
    }

    @Test
    public void whenPersistenceIsEnabled_addSchedulerToPeriodicTasks_andKeepTaskEnabled() {
        exportScheduler.onPersistenceEnabled();

        verify(service).addTask(exportScheduler);
        assertFalse(exportScheduler.isFinished());
    }

    @Test
    public void whenPersistenceIsDisabled_disableTask() {
        exportScheduler.onPersistenceDisabled();

        assertTrue(exportScheduler.isFinished());
    }

    @Test
    public void whenEnabledAndRunning_exportAllSignals() throws IOException {
        when(signalDiskExporter.exportBatchOfEach()).thenReturn(true).thenReturn(true).thenReturn(false);

        exportScheduler.onPeriodicTaskRun();

        verify(signalDiskExporter, times(3)).exportBatchOfEach();
    }

    @Test
    public void whenDisabledAndRunning_doNotExportSignals() throws IOException {
        exportScheduler.onPersistenceDisabled();

        exportScheduler.onPeriodicTaskRun();

        verify(signalDiskExporter, never()).exportBatchOfEach();
    }

    @Test
    public void whenPersistenceIsReEnabled_reEnableTheScheduler() {
        exportScheduler.onPersistenceEnabled();
        exportScheduler.onPersistenceDisabled();
        clearInvocations(service);

        exportScheduler.onPersistenceEnabled();

        verify(service).addTask(exportScheduler);
        assertFalse(exportScheduler.isFinished());
    }

    @Test
    public void provideTimeoutInMillis() {
        assertEquals(DELAY_TIME_IN_MILLIS, exportScheduler.getMinDelayBeforeNextRunInMillis());
    }
}