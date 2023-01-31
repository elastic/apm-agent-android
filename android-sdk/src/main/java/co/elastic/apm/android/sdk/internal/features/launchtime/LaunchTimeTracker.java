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
package co.elastic.apm.android.sdk.internal.features.launchtime;

import co.elastic.apm.android.common.internal.logging.Elog;

public final class LaunchTimeTracker {
    private static long initialTimeInNanos = 0;
    private static long launchTimeInNanos = 0;
    private static boolean finalizedTracking = false;
    private static boolean timeAlreadyQueried = false;

    static void startTimer() {
        initialTimeInNanos = System.nanoTime();
    }

    public static boolean stopTimer() {
        if (finalizedTracking) {
            return false;
        }

        Elog.getLogger().info("Stopping app launch time tracker");
        launchTimeInNanos = System.nanoTime() - initialTimeInNanos;
        initialTimeInNanos = 0;
        finalizedTracking = true;

        return true;
    }

    public static long getElapsedTimeInNanos() {
        if (!finalizedTracking) {
            throw new IllegalStateException("No tracked time available");
        }
        if (timeAlreadyQueried) {
            throw new IllegalStateException("Launch time already queried");
        }

        Elog.getLogger().info("Retrieving app launch time tracker");

        long time = launchTimeInNanos;
        launchTimeInNanos = 0;
        timeAlreadyQueried = true;

        return time;
    }
}
