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

import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.internal.opentelemetry.clock.ElapsedTimeOffsetClock
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import io.opentelemetry.sdk.common.Clock
import java.util.concurrent.TimeUnit

internal class ElasticClockManager internal constructor(
    serviceManager: ServiceManager,
    private val sntpClient: SntpClient,
    private val systemTimeProvider: SystemTimeProvider
) {
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }
    private val clock: ElapsedTimeOffsetClock by lazy {
        ElapsedTimeOffsetClock(systemTimeProvider)
    }
    private val logger = Elog.getLogger()

    init {
        logger.debug(
            "Initializing ElasticClockManager with sntpClient: {} and systemTimeProvider: {}",
            sntpClient,
            systemTimeProvider
        )
    }

    internal fun initialize() {
        backgroundWorkService.schedulePeriodicTask(SyncHandler(), 1, TimeUnit.MINUTES)
    }

    internal fun close() {
        sntpClient.close()
    }

    internal fun getClock(): Clock {
        return clock
    }

    private fun syncClock() {
        logger.debug("Starting clock sync.")
        try {
            val response =
                sntpClient.fetchTimeOffset(systemTimeProvider.getElapsedRealTime() + TIME_REFERENCE)
            if (response is SntpClient.Response.Success) {
                clock.setOffset(TIME_REFERENCE + response.offsetMillis)
                logger.debug(
                    "ElasticClockManager successfully fetched time offset: {}",
                    response.offsetMillis
                )
            } else {
                logger.debug("ElasticClockManager error: {}", response)
            }
        } catch (e: Exception) {
            logger.debug("ElasticClockManager exception", e)
        }
    }

    companion object {
        private const val TIME_REFERENCE = 1577836800000L
    }

    private inner class SyncHandler : Runnable {
        override fun run() {
            syncClock()
        }
    }
}