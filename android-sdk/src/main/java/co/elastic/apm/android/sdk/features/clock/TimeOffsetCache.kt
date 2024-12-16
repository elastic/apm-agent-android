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
import java.util.concurrent.atomic.AtomicLong

internal class TimeOffsetCache(
    private val preferencesService: PreferencesService,
    private val systemTimeProvider: SystemTimeProvider
) {
    private val timeOffset = AtomicLong(NO_VALUE)
    private val lastUpdateTime = AtomicLong(NO_VALUE)

    init {
        timeOffset.set(preferencesService.retrieveLong(TIME_OFFSET_KEY, NO_VALUE))
        lastUpdateTime.set(preferencesService.retrieveLong(LAST_UPDATE_KEY, NO_VALUE))
    }

    fun getTimeOffset(): Long? {
        return checkNoValue(timeOffset.get())
    }

    fun getLastUpdateTimeMillis(): Long? {
        return checkNoValue(lastUpdateTime.get())
    }

    fun storeTimeOffset(timeOffset: Long) {
        preferencesService.store(LAST_UPDATE_KEY, systemTimeProvider.getCurrentTimeMillis())
        preferencesService.store(TIME_OFFSET_KEY, timeOffset)
    }

    private fun checkNoValue(value: Long): Long? {
        return if (value == NO_VALUE) null else value
    }

    companion object {
        private const val TIME_OFFSET_KEY = "time_offset"
        private const val LAST_UPDATE_KEY = "time_offset_last_update"
        private const val NO_VALUE = -1L
    }
}