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

import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;

public abstract class ManagedPeriodicTask implements PeriodicTask {
    private final SystemTimeProvider timeProvider;
    private long lastTimeItRan = 0;

    public ManagedPeriodicTask() {
        this(SystemTimeProvider.get());
    }

    protected ManagedPeriodicTask(SystemTimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public final void runTask() {
        onTaskRun();
        lastTimeItRan = timeProvider.getCurrentTimeMillis();
    }

    /**
     * Runs a task when it's due.
     */
    protected abstract void onTaskRun();

    /**
     * Returns the minimum amount of milliseconds that need to pass before this task gets to run again.
     * The actual time before running this task can be higher than the one provided in here,
     * given that tasks are run from the {@link PeriodicWorkService} class which has its own internal
     * delay between iterations.
     */
    public abstract long getMinDelayBeforeNextRunInMillis();

    @Override
    public final boolean shouldRunTask() {
        if (lastTimeItRan < 1) {
            return true;
        }
        return timeProvider.getCurrentTimeMillis() >= (lastTimeItRan + getMinDelayBeforeNextRunInMillis());
    }
}
