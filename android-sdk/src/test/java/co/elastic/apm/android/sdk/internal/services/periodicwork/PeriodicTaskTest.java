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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;

public class PeriodicTaskTest {
    private TestPeriodicTask periodicTask;
    private SystemTimeProvider timeProvider;
    private static final long INITIAL_TIME_IN_MILLIS = 1000;

    @Before
    public void setUp() {
        timeProvider = mock(SystemTimeProvider.class);
        doReturn(INITIAL_TIME_IN_MILLIS).when(timeProvider).getCurrentTimeMillis();
        periodicTask = new TestPeriodicTask(timeProvider);
    }

    @Test
    public void verifyFirstRun() {
        assertTrue(periodicTask.runPeriodicTask());

        assertEquals(1, periodicTask.timesRan);
    }

    @Test
    public void verifyContinuousRuns() {
        periodicTask.millisToWaitBeforeNextRun = 10_000;
        assertTrue(periodicTask.runPeriodicTask());

        // Timeout has not completed, so it should not run next time.
        assertFalse(periodicTask.runPeriodicTask());

        // Fast forward to just before the timeout is done.
        fastForwardTimeByMillis(9_999);

        // It should still not run.
        assertFalse(periodicTask.runPeriodicTask());

        // Fast forward to just in time for the next run.
        fastForwardTimeByMillis(1);

        // Now it should run again.
        assertTrue(periodicTask.runPeriodicTask());

        assertEquals(2, periodicTask.timesRan);
    }

    private void fastForwardTimeByMillis(long millis) {
        doReturn(timeProvider.getCurrentTimeMillis() + millis).when(timeProvider).getCurrentTimeMillis();
    }

    private static class TestPeriodicTask extends PeriodicTask {
        private long millisToWaitBeforeNextRun;
        private int timesRan;

        public TestPeriodicTask(SystemTimeProvider timeProvider) {
            super(timeProvider);
        }

        @Override
        protected void onPeriodicTaskRun() {
            timesRan++;
        }

        @Override
        protected long getMinDelayBeforeNextRunInMillis() {
            return millisToWaitBeforeNextRun;
        }

        @Override
        public boolean isFinished() {
            return false;
        }
    }
}