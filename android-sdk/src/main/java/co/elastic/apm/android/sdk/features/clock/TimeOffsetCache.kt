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

import co.elastic.apm.android.sdk.internal.services.kotlin.preferences.PreferencesService
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

internal class TimeOffsetCache(
    private val preferencesService: PreferencesService,
    private val systemTimeProvider: SystemTimeProvider
) {
    private val timeOffset = AtomicLong(NO_VALUE)
    private val offsetExpireTime = AtomicLong(NO_VALUE)

    init {
        timeOffset.set(preferencesService.retrieveLong(TIME_OFFSET_KEY, NO_VALUE))
        offsetExpireTime.set(preferencesService.retrieveLong(EXPIRE_TIME_KEY, NO_VALUE))
    }

    fun getTimeOffset(): Long? {
        return checkNoValue(timeOffset.get())
    }

    fun isCurrentOffsetValid(): Boolean {
        return checkNoValue(offsetExpireTime.get())?.let {
            systemTimeProvider.getCurrentTimeMillis() < it
        } ?: false
    }

    fun storeTimeOffset(value: Long) {
        val expireTime = systemTimeProvider.getCurrentTimeMillis() + CACHE_MAX_VALID_TIME
        timeOffset.set(value)
        offsetExpireTime.set(expireTime)
        preferencesService.store(EXPIRE_TIME_KEY, expireTime)
        preferencesService.store(TIME_OFFSET_KEY, value)
    }

    private fun checkNoValue(value: Long): Long? {
        return if (value == NO_VALUE) null else value
    }

    fun clear() {
        preferencesService.remove(TIME_OFFSET_KEY)
        preferencesService.remove(EXPIRE_TIME_KEY)
    }

    companion object {
        private const val TIME_OFFSET_KEY = "time_offset"
        private const val EXPIRE_TIME_KEY = "time_offset_expire_time"
        private const val NO_VALUE = -1L
        private val CACHE_MAX_VALID_TIME = TimeUnit.HOURS.toMillis(24)
    }
}