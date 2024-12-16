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
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

internal class TimeOffsetManager private constructor(
    serviceManager: ServiceManager,
    private val systemTimeProvider: SystemTimeProvider,
    private val sntpClient: SntpClient,
    private val timeOffsetCache: TimeOffsetCache
) {
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }
    private val timeOffset =
        AtomicLong(systemTimeProvider.getCurrentTimeMillis() - systemTimeProvider.getElapsedRealTime())
    private lateinit var listener: Listener
    private val logger = Elog.getLogger()

    init {
        logger.debug(
            "Initializing ElasticClockManager with sntpClient: {} and systemTimeProvider: {}",
            sntpClient,
            systemTimeProvider
        )
    }

    internal fun initialize() {
        timeOffsetCache.getTimeOffset()?.let { setTimeOffset(it) }
        backgroundWorkService.schedulePeriodicTask(SyncHandler(), 1, TimeUnit.MINUTES)
    }

    internal fun getTimeOffset(): Long {
        return timeOffset.get()
    }

    internal fun sync() {
        logger.debug("Starting clock sync.")
        try {
            val response =
                sntpClient.fetchTimeOffset(systemTimeProvider.getElapsedRealTime() + TIME_REFERENCE)
            if (response is SntpClient.Response.Success) {
                setTimeOffset(TIME_REFERENCE + response.offsetMillis)
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

    private fun setTimeOffset(offset: Long) {
        timeOffset.set(offset)
        timeOffsetCache.storeTimeOffset(offset)
        listener.onTimeOffsetChanged()
    }

    internal fun close() {
        sntpClient.close()
    }

    internal fun setListener(listener: Listener) {
        this.listener = listener
    }

    companion object {
        private const val TIME_REFERENCE = 1577836800000L

        internal fun create(
            serviceManager: ServiceManager,
            systemTimeProvider: SystemTimeProvider,
            sntpClient: SntpClient
        ): TimeOffsetManager {
            return TimeOffsetManager(
                serviceManager, systemTimeProvider, sntpClient,
                TimeOffsetCache(serviceManager.getPreferencesService(), systemTimeProvider)
            )
        }
    }

    internal interface Listener {
        fun onTimeOffsetChanged()
    }

    private inner class SyncHandler : Runnable {
        override fun run() {
            sync()
        }
    }
}