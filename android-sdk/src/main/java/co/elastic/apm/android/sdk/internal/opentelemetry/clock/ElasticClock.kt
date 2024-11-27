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
package co.elastic.apm.android.sdk.internal.opentelemetry.clock

import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.internal.services.periodicwork.ManagedPeriodicTask
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import io.opentelemetry.sdk.common.Clock
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class ElasticClock(
    private val sntpClient: SntpClient,
    private val systemTimeProvider: SystemTimeProvider
) : ManagedPeriodicTask(), Clock {
    private val offsetTime =
        AtomicLong(systemTimeProvider.currentTimeMillis - systemTimeProvider.elapsedRealTime)

    companion object {
        @JvmStatic
        fun create(): ElasticClock {
            return ElasticClock(SntpClient.create(), SystemTimeProvider.get())
        }

        private val POLLING_INTERVAL = TimeUnit.MINUTES.toMillis(1)
        private const val TIME_REFERENCE = 1577836800000L
        private const val MILLIS_TIMES_TO_NANOS = 1_000_000L
    }

    override fun now(): Long {
        return (offsetTime.get() + systemTimeProvider.elapsedRealTime) * MILLIS_TIMES_TO_NANOS
    }

    override fun nanoTime(): Long {
        return systemTimeProvider.nanoTime
    }

    override fun onTaskRun() {
        try {
            val response =
                sntpClient.fetchTimeOffset(systemTimeProvider.elapsedRealTime + TIME_REFERENCE)
            if (response is SntpClient.Response.Success) {
                offsetTime.set(TIME_REFERENCE + response.offsetMillis)
                Elog.getLogger().debug(
                    "ElasticClock successfully fetched time offset: {}",
                    response.offsetMillis
                )
            } else {
                Elog.getLogger().debug("ElasticClock error: {}", response)
            }
        } catch (e: Exception) {
            Elog.getLogger().debug("ElasticClock task exception", e)
        }
    }

    override fun getMinDelayBeforeNextRunInMillis(): Long {
        return POLLING_INTERVAL
    }

    override fun isTaskFinished(): Boolean {
        return false
    }
}
