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

import co.elastic.otel.android.internal.time.SystemTimeProvider
import io.opentelemetry.sdk.common.Clock
import java.util.concurrent.TimeUnit

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class SystemTimeClock(private val systemTimeProvider: SystemTimeProvider) : Clock {

    override fun now(): Long {
        return TimeUnit.MILLISECONDS.toNanos(systemTimeProvider.getCurrentTimeMillis())
    }

    override fun nanoTime(): Long {
        return systemTimeProvider.getNanoTime()
    }
}