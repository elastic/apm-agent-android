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
package co.elastic.apm.android.sdk.features.clock

import co.elastic.apm.android.sdk.internal.opentelemetry.clock.ElapsedTimeOffsetClock
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import io.opentelemetry.sdk.common.Clock
import java.util.concurrent.atomic.AtomicBoolean

internal class ElasticClockManager private constructor(
    systemTimeProvider: SystemTimeProvider,
    private val timeOffsetManager: RemoteTimeOffsetManager
) : RemoteTimeOffsetManager.Listener {
    private val elapsedTimeOffsetClock = ElapsedTimeOffsetClock(systemTimeProvider)
    private val systemTimeClock = SystemTimeClock(systemTimeProvider)
    private val clock = MutableClock(systemTimeClock)
    private val usingRemoteTime = AtomicBoolean(false)

    companion object {
        internal fun create(
            serviceManager: ServiceManager,
            systemTimeProvider: SystemTimeProvider,
            sntpClient: SntpClient
        ): ElasticClockManager {
            val timeOffsetManager =
                RemoteTimeOffsetManager.create(serviceManager, systemTimeProvider, sntpClient)
            val clockManager = ElasticClockManager(systemTimeProvider, timeOffsetManager)
            timeOffsetManager.setListener(clockManager)
            return clockManager
        }
    }

    internal fun initialize() {
        timeOffsetManager.initialize()
    }

    internal fun close() {
        timeOffsetManager.close()
    }

    internal fun getClock(): Clock {
        return clock
    }

    internal fun getTimeOffsetManager(): RemoteTimeOffsetManager {
        return timeOffsetManager
    }

    override fun onTimeOffsetChanged() {
        val timeOffset = timeOffsetManager.getTimeOffset()
        if (timeOffset != null) {
            elapsedTimeOffsetClock.setOffset(timeOffset)
            if (usingRemoteTime.compareAndSet(false, true)) {
                clock.setDelegate(elapsedTimeOffsetClock)
            }
        } else if (usingRemoteTime.compareAndSet(true, false)) {
            clock.setDelegate(systemTimeClock)
        }
    }
}