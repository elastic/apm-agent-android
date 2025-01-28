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
package co.elastic.otel.android.launchtime.internal

internal object LaunchTimeTracker {
    private var initialTimeInNanos: Long = 0
    private var launchTimeInNanos: Long = 0
    private var finalizedTracking = false
    private var timeAlreadyQueried = false

    fun startTimer() {
        initialTimeInNanos = System.nanoTime()
    }

    fun resetForTest() {
        finalizedTracking = false
        timeAlreadyQueried = false
    }

    fun stopTimer(): Boolean {
        if (finalizedTracking) {
            return false
        }

        launchTimeInNanos = System.nanoTime() - initialTimeInNanos
        initialTimeInNanos = 0
        finalizedTracking = true

        return true
    }

    val elapsedTimeInNanos: Long
        get() {
            check(finalizedTracking) { "No tracked time available" }
            check(!timeAlreadyQueried) { "Launch time already queried" }

            val time = launchTimeInNanos
            launchTimeInNanos = 0
            timeAlreadyQueried = true

            return time
        }
}
