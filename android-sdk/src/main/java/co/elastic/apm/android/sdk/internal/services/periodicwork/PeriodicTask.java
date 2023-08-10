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

public abstract class PeriodicTask {
    private final SystemTimeProvider timeProvider;
    private long lastTimeItRan = 0;

    public PeriodicTask() {
        this(SystemTimeProvider.get());
    }

    protected PeriodicTask(SystemTimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Calls {@link #onPeriodicTaskRun()} if the task is due to be run, noop otherwise.
     *
     * @return true when the task was run, false otherwise.
     */
    public final boolean runPeriodicTask() {
        if (isReadyToRun()) {
            onPeriodicTaskRun();
            lastTimeItRan = timeProvider.getCurrentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Runs a task when it's due.
     */
    protected abstract void onPeriodicTaskRun();

    /**
     * Returns the amount of milliseconds that need to pass before this task gets to run again.
     */
    protected abstract long getMillisToWaitBeforeNextRun();

    /**
     * Indicates whether this task needs to keep running in future iterations or not.
     *
     * @return false if this task needs to be called again in the future, true otherwise.
     */
    public abstract boolean isFinished();

    private boolean isReadyToRun() {
        if (lastTimeItRan < 1) {
            return true;
        }
        return timeProvider.getCurrentTimeMillis() >= (lastTimeItRan + getMillisToWaitBeforeNextRun());
    }
}
