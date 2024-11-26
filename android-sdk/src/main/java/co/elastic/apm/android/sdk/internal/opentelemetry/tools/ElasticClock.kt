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
package co.elastic.apm.android.sdk.internal.opentelemetry.tools

import androidx.annotation.VisibleForTesting
import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.internal.services.periodicwork.ManagedPeriodicTask
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import io.opentelemetry.sdk.common.Clock
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class ElasticClock @VisibleForTesting constructor(
    private val sntpClient: SntpClient,
    private val systemTimeProvider: SystemTimeProvider
) : ManagedPeriodicTask(), Clock {
    private val offsetMillis = AtomicLong(0)

    override fun now(): Long {
        return TimeUnit.MILLISECONDS.toNanos(systemTimeProvider.currentTimeMillis + offsetMillis.get())
    }

    override fun nanoTime(): Long {
        return systemTimeProvider.nanoTime
    }

    override fun onTaskRun() {
        try {
            val response = sntpClient.fetchTimeOffset(systemTimeProvider::getCurrentTimeMillis)
            if (response is SntpClient.Response.Success) {
                offsetMillis.set(response.offsetMillis)
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

    companion object {
        private val POLLING_INTERVAL = TimeUnit.MINUTES.toMillis(1)

        @JvmStatic
        fun create(): ElasticClock {
            return ElasticClock(SntpClient.create(), SystemTimeProvider.get())
        }
    }
}
