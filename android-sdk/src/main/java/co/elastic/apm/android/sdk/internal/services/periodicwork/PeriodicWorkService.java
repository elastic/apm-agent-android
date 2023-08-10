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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.DaemonThreadFactory;

public class PeriodicWorkService implements Service, Runnable {
    private final Set<PeriodicTask> tasks = new HashSet<>();
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;
    private static final long DELAY_BETWEEN_WORK_RUNS_IN_MILLIS = 5 * 1000; // 5 seconds

    public synchronized void addTask(PeriodicTask task) {
        tasks.add(task);
    }

    @Override
    public void start() {
        Elog.getLogger().debug("Starting PeriodicWorkService");
        if (isStopped.get()) {
            throw new IllegalStateException("The periodic work service has been stopped");
        }
        executorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
        scheduleNextWorkRun();
    }

    @Override
    public void stop() {
        isStopped.set(true);
    }

    @Override
    public String name() {
        return Names.PERIODIC_WORK;
    }

    @Override
    public void run() {
        synchronized (this) {
            for (PeriodicTask task : tasks) {
                try {
                    task.execute();
                } catch (Throwable t) {
                    Elog.getLogger().error("Failed to execute periodic task", t);
                }
            }
        }
        if (!isStopped.get()) {
            scheduleNextWorkRun();
        }
    }

    private void scheduleNextWorkRun() {
        executorService.schedule(this, DELAY_BETWEEN_WORK_RUNS_IN_MILLIS, TimeUnit.MILLISECONDS);
    }
}
