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
package co.elastic.apm.android.sdk.internal.services.periodicwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.internal.services.Service;

public class PeriodicWorkServiceTest {
    private ScheduledExecutorService executorService;
    private PeriodicWorkService periodicWorkService;

    @Before
    public void setUp() {
        executorService = mock(ScheduledExecutorService.class);
        periodicWorkService = new PeriodicWorkService(executorService);
    }

    @Test
    public void whenInitializing_executeFirstIterationRightAway() {
        assertFalse(periodicWorkService.isInitialized());
        verifyNoInteractions(executorService);
        periodicWorkService.initialize();

        assertTrue(periodicWorkService.isInitialized());
        verify(executorService).execute(periodicWorkService);

        // Check that this can be done only once:
        periodicWorkService.initialize();
        verifyNoMoreInteractions(executorService);
    }

    @Test
    public void whenInitializingAfterStopped_throwError() {
        periodicWorkService.stop();

        try {
            periodicWorkService.initialize();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void verifyServiceName() {
        assertEquals(Service.Names.PERIODIC_WORK, periodicWorkService.name());
    }

    @Test
    public void whenRunning_executeTasks() {
        PeriodicTask task = mock(PeriodicTask.class);
        PeriodicTask task2 = mock(PeriodicTask.class);
        periodicWorkService.addTask(task);
        periodicWorkService.addTask(task2);

        periodicWorkService.run();

        verify(task).runPeriodicTask();
        verify(task2).runPeriodicTask();
        assertTrue(periodicWorkService.getTasks().contains(task));
        assertTrue(periodicWorkService.getTasks().contains(task2));
    }

    @Test
    public void whenATaskFinishesAfterRunning_removeThemForNextIteration() {
        PeriodicTask task = mock(PeriodicTask.class);
        doReturn(true).when(task).runPeriodicTask();
        doReturn(true).when(task).isFinished();
        PeriodicTask task2 = mock(PeriodicTask.class);
        periodicWorkService.addTask(task);
        periodicWorkService.addTask(task2);

        periodicWorkService.run();

        verify(task).runPeriodicTask();
        verify(task2).runPeriodicTask();
        assertFalse(periodicWorkService.getTasks().contains(task));
        assertTrue(periodicWorkService.getTasks().contains(task2));
    }

    @Test
    public void afterRunning_ifNotStopped_scheduleNextRun() {
        periodicWorkService.run();

        verify(executorService).schedule(periodicWorkService, 5_000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void afterRunning_ifStopped_doNotScheduleNextRun() {
        periodicWorkService.stop();

        periodicWorkService.run();

        verify(executorService, never()).schedule(eq(periodicWorkService), anyLong(), any());
    }
}