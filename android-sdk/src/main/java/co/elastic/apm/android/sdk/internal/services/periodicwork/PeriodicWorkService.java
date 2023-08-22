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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.DaemonThreadFactory;

public class PeriodicWorkService implements Service, Runnable {
    private final CopyOnWriteArrayList<PeriodicTask> tasks = new CopyOnWriteArrayList<>();
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final ScheduledExecutorService executorService;
    private static final long DELAY_BETWEEN_ITERATIONS_IN_MILLIS = 5 * 1000; // 5 seconds

    public void addTask(PeriodicTask task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    public PeriodicWorkService() {
        this(Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory()));
    }

    PeriodicWorkService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        isStopped.set(true);
    }

    @Override
    public String name() {
        return Names.PERIODIC_WORK;
    }

    public void initialize() {
        verifyNotStopped();
        if (isInitialized.compareAndSet(false, true)) {
            executorService.execute(this);
        }
    }

    public boolean isInitialized() {
        return isInitialized.get();
    }

    public List<PeriodicTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    @Override
    public void run() {
        List<PeriodicTask> removeTasks = new ArrayList<>();
        for (PeriodicTask task : tasks) {
            try {
                if (task.shouldRunTask()) {
                    task.runTask();
                }
                if (task.isTaskFinished()) {
                    removeTasks.add(task);
                }
            } catch (Throwable t) {
                Elog.getLogger().error("Failed to execute periodic task", t);
            }
        }
        if (!removeTasks.isEmpty()) {
            Elog.getLogger().debug("Removing periodic tasks: " + removeTasks);
            tasks.removeAll(removeTasks);
        }

        if (!isStopped.get()) {
            scheduleNextWorkRun();
        }
    }

    private void scheduleNextWorkRun() {
        executorService.schedule(this, DELAY_BETWEEN_ITERATIONS_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void verifyNotStopped() {
        if (isStopped.get()) {
            throw new IllegalStateException("The periodic work service has been stopped");
        }
    }
}
