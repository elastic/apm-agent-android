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
package co.elastic.otel.android.internal.features.clock

import androidx.annotation.GuardedBy
import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.services.preferences.PreferencesService
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.time.ntp.SntpClient
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class RemoteTimeOffsetManager private constructor(
    serviceManager: ServiceManager,
    private val systemTimeProvider: SystemTimeProvider,
    private val sntpClient: SntpClient,
    private val timeOffsetCache: TimeOffsetCache
) {
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }
    private val logger = Elog.getLogger()
    private val writeLock = Any()
    private lateinit var listener: Listener

    @GuardedBy("writeLock")
    private var timeOffset: AtomicReference<TimeOffset?> = AtomicReference(null)

    init {
        logger.debug(
            "Initializing ElasticClockManager with sntpClient: {} and systemTimeProvider: {}",
            sntpClient,
            systemTimeProvider
        )
    }

    internal fun initialize() {
        timeOffsetCache.retrieveTimeOffset()?.let {
            if (!checkIfExpired(it)) {
                setTimeOffset(it)
            }
        }
        backgroundWorkService.schedulePeriodicTask(1, TimeUnit.MINUTES, SyncHandler())
    }

    internal fun getTimeOffset(): Long? {
        return timeOffset.get()?.offset
    }

    internal fun sync() {
        logger.debug("Starting clock sync.")
        try {
            val response =
                sntpClient.fetchTimeOffset(systemTimeProvider.getElapsedRealTime() + TIME_REFERENCE)
            if (response is SntpClient.Response.Success) {
                val offset = TIME_REFERENCE + response.offsetMillis
                val timeOffset =
                    TimeOffset(
                        offset,
                        calculateExpireTime()
                    )
                setTimeOffset(timeOffset)
                storeTimeOffsetInCache(timeOffset)
                logger.debug(
                    "ElasticClockManager successfully fetched time offset: {}",
                    response.offsetMillis
                )
            } else {
                checkIfExpired(timeOffset.get())
                logger.debug("ElasticClockManager error: {}", response)
            }
        } catch (e: Exception) {
            logger.debug("ElasticClockManager exception", e)
        }
    }

    private fun checkIfExpired(timeOffset: TimeOffset?): Boolean {
        synchronized(writeLock) {
            if (timeOffset != null && hasExpired(timeOffset.expireTimeMillis)) {
                clearTimeOffset()
                return true
            }
        }
        return false
    }

    private fun hasExpired(expireTimeMillis: Long): Boolean {
        return systemTimeProvider.getCurrentTimeMillis() >= expireTimeMillis
    }

    private fun clearTimeOffset() {
        setTimeOffset(null)
        timeOffsetCache.clear()
    }

    private fun calculateExpireTime(): Long {
        return systemTimeProvider.getCurrentTimeMillis() + CACHE_MAX_VALID_TIME
    }

    private fun storeTimeOffsetInCache(timeOffset: TimeOffset) {
        timeOffsetCache.storeTimeOffset(timeOffset)
    }

    private fun setTimeOffset(value: TimeOffset?) = synchronized(writeLock) {
        if (timeOffset.get() != value) {
            timeOffset.set(value)
            notifyChange()
        }
    }

    private fun notifyChange() {
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
        private val CACHE_MAX_VALID_TIME = TimeUnit.HOURS.toMillis(24)

        internal fun create(
            serviceManager: ServiceManager,
            systemTimeProvider: SystemTimeProvider,
            sntpClient: SntpClient
        ): RemoteTimeOffsetManager {
            return RemoteTimeOffsetManager(
                serviceManager, systemTimeProvider, sntpClient,
                TimeOffsetCache(
                    serviceManager.getPreferencesService()
                )
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

    internal data class TimeOffset(val offset: Long, val expireTimeMillis: Long)

    internal class TimeOffsetCache(
        private val preferencesService: PreferencesService
    ) {
        fun retrieveTimeOffset(): TimeOffset? {
            return checkNoValue(
                preferencesService.retrieveLong(
                    KEY_TIME_OFFSET,
                    NO_VALUE
                )
            )?.let { offset ->
                val expireTime =
                    checkNoValue(preferencesService.retrieveLong(
                        KEY_EXPIRE_TIME,
                        NO_VALUE
                    ))
                        ?: throw IllegalStateException()
                TimeOffset(
                    offset,
                    expireTime
                )
            }
        }

        fun storeTimeOffset(timeOffset: TimeOffset) {
            preferencesService.store(KEY_TIME_OFFSET, timeOffset.offset)
            preferencesService.store(KEY_EXPIRE_TIME, timeOffset.expireTimeMillis)
        }

        fun clear() {
            preferencesService.remove(KEY_TIME_OFFSET)
            preferencesService.remove(KEY_EXPIRE_TIME)
        }

        private fun checkNoValue(value: Long): Long? {
            return if (value == NO_VALUE) null else value
        }

        companion object {
            private const val KEY_TIME_OFFSET = "time_offset"
            private const val KEY_EXPIRE_TIME = "time_offset_expire_time"
            private const val NO_VALUE = -1L
        }
    }
}