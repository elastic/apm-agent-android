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
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import io.opentelemetry.sdk.common.Clock
import java.util.concurrent.atomic.AtomicLong

internal class ElapsedTimeOffsetClock(
    initialTimeOffset: Long,
    private val systemTimeProvider: SystemTimeProvider
) : Clock {
    private val offsetTime = AtomicLong(initialTimeOffset)
    private val logger = Elog.getLogger()

    override fun now(): Long {
        return (offsetTime.get() + systemTimeProvider.getElapsedRealTime()) * MILLIS_TIMES_TO_NANOS
    }

    override fun nanoTime(): Long {
        return systemTimeProvider.getNanoTime()
    }

    internal fun setOffset(value: Long) {
        logger.debug("Setting time offset: {}", value)
        offsetTime.set(value)
    }

    companion object {
        private const val MILLIS_TIMES_TO_NANOS = 1_000_000L
    }
}