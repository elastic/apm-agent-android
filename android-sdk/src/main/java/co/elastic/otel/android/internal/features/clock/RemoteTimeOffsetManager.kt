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

internal class RemoteTimeOffsetManager private constructor(
    serviceManager: ServiceManager,
    private val systemTimeProvider: SystemTimeProvider,
    private val sntpClient: SntpClient,
    private val timeOffsetCache: co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache
) {
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }
    private val logger = Elog.getLogger()
    private val writeLock = Any()
    private lateinit var listener: co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.Listener

    @GuardedBy("writeLock")
    private var timeOffset: AtomicReference<co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffset?> = AtomicReference(null)

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
                sntpClient.fetchTimeOffset(systemTimeProvider.getElapsedRealTime() + co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.Companion.TIME_REFERENCE)
            if (response is SntpClient.Response.Success) {
                val offset = co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.Companion.TIME_REFERENCE + response.offsetMillis
                val timeOffset =
                    co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffset(
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

    private fun checkIfExpired(timeOffset: co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffset?): Boolean {
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
        return systemTimeProvider.getCurrentTimeMillis() + co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.Companion.CACHE_MAX_VALID_TIME
    }

    private fun storeTimeOffsetInCache(timeOffset: co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffset) {
        timeOffsetCache.storeTimeOffset(timeOffset)
    }

    private fun setTimeOffset(value: co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffset?) = synchronized(writeLock) {
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

    internal fun setListener(listener: co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.Listener) {
        this.listener = listener
    }

    companion object {
        private const val TIME_REFERENCE = 1577836800000L
        private val CACHE_MAX_VALID_TIME = TimeUnit.HOURS.toMillis(24)

        internal fun create(
            serviceManager: ServiceManager,
            systemTimeProvider: SystemTimeProvider,
            sntpClient: SntpClient
        ): co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager {
            return co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager(
                serviceManager, systemTimeProvider, sntpClient,
                co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache(
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
        fun retrieveTimeOffset(): co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffset? {
            return checkNoValue(
                preferencesService.retrieveLong(
                    co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache.Companion.KEY_TIME_OFFSET,
                    co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache.Companion.NO_VALUE
                )
            )?.let { offset ->
                val expireTime =
                    checkNoValue(preferencesService.retrieveLong(
                        co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache.Companion.KEY_EXPIRE_TIME,
                        co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache.Companion.NO_VALUE
                    ))
                        ?: throw IllegalStateException()
                co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffset(
                    offset,
                    expireTime
                )
            }
        }

        fun storeTimeOffset(timeOffset: co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffset) {
            preferencesService.store(co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache.Companion.KEY_TIME_OFFSET, timeOffset.offset)
            preferencesService.store(co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache.Companion.KEY_EXPIRE_TIME, timeOffset.expireTimeMillis)
        }

        fun clear() {
            preferencesService.remove(co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache.Companion.KEY_TIME_OFFSET)
            preferencesService.remove(co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache.Companion.KEY_EXPIRE_TIME)
        }

        private fun checkNoValue(value: Long): Long? {
            return if (value == co.elastic.otel.android.internal.features.clock.RemoteTimeOffsetManager.TimeOffsetCache.Companion.NO_VALUE) null else value
        }

        companion object {
            private const val KEY_TIME_OFFSET = "time_offset"
            private const val KEY_EXPIRE_TIME = "time_offset_expire_time"
            private const val NO_VALUE = -1L
        }
    }
}