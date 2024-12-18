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

import co.elastic.apm.android.sdk.features.exportergate.latch.Latch
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.tools.AttributesOverrideLogRecordData
import co.elastic.apm.android.sdk.tools.AttributesOverrideSpanData
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import co.elastic.apm.android.sdk.tools.interceptor.MutableInterceptor
import co.elastic.apm.android.sdk.tools.provider.Provider
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.trace.data.SpanData
import java.util.concurrent.atomic.AtomicBoolean

class ClockExporterGateManager private constructor(
    systemTimeProvider: SystemTimeProvider,
    private val timeOffsetNanosProvider: Provider<Long?>
) {
    private val gateOpened = AtomicBoolean(false)
    private val globalAttributesInterceptor =
        MutableInterceptor(ElapsedTimeAttributeInterceptor(systemTimeProvider))
    private var spanGateProcessingInterceptor: SpanGateProcessingInterceptor? =
        SpanGateProcessingInterceptor()
    private var logRecordGateProcessingInterceptor: LogRecordGateProcessingInterceptor? =
        LogRecordGateProcessingInterceptor()
    private var latch: Latch? = null

    internal fun getAttributesInterceptor(): Interceptor<Attributes> {
        return globalAttributesInterceptor
    }

    internal fun getSpanGateProcessingInterceptor(): SpanGateProcessingInterceptor {
        return spanGateProcessingInterceptor!!
    }

    internal fun getLogRecordGateProcessingInterceptor(): LogRecordGateProcessingInterceptor {
        return logRecordGateProcessingInterceptor!!
    }

    internal fun onRemoteClockSet() {
        if (gateOpened.compareAndSet(false, true)) {
            latch?.open().also { latch = null }
            globalAttributesInterceptor.setDelegate(Interceptor.noop())
            spanGateProcessingInterceptor = null
            logRecordGateProcessingInterceptor = null
        }
    }

    companion object {
        internal fun create(
            systemTimeProvider: SystemTimeProvider,
            timeOffsetNanosProvider: Provider<Long?>,
            exporterGateLatch: Latch
        ): ClockExporterGateManager {
            val manager = ClockExporterGateManager(systemTimeProvider, timeOffsetNanosProvider)
            manager.latch = exporterGateLatch
            return manager
        }

        private val ATTRIBUTE_KEY_CREATION_ELAPSED_TIME =
            AttributeKey.longKey("internal.elastic.creation_elapsed_time")
    }

    inner class SpanGateProcessingInterceptor : Interceptor<SpanData> {

        override fun intercept(item: SpanData): SpanData {
            val elapsedStartTime = item.attributes.get(ATTRIBUTE_KEY_CREATION_ELAPSED_TIME)
            if (elapsedStartTime != null) {
                timeOffsetNanosProvider.get()?.let {
                    return TimeUpdatedSpanData.create(item, elapsedStartTime, it)
                }
            }
            return item
        }
    }

    inner class LogRecordGateProcessingInterceptor : Interceptor<LogRecordData> {

        override fun intercept(item: LogRecordData): LogRecordData {
            val creationElapsedTime = item.attributes.get(ATTRIBUTE_KEY_CREATION_ELAPSED_TIME)
            if (creationElapsedTime != null) {
                timeOffsetNanosProvider.get()?.let {
                    return TimeUpdatedLogRecordData.create(item, creationElapsedTime, it)
                }
            }
            return item
        }
    }

    inner class ElapsedTimeAttributeInterceptor(private val systemTimeProvider: SystemTimeProvider) :
        Interceptor<Attributes> {

        override fun intercept(item: Attributes): Attributes {
            return Attributes.builder().putAll(item)
                .put(ATTRIBUTE_KEY_CREATION_ELAPSED_TIME, systemTimeProvider.getElapsedRealTime())
                .build()
        }
    }

    private class TimeUpdatedLogRecordData private constructor(
        delegate: LogRecordData,
        attributes: Attributes,
        totalAttributeCount: Int,
        private val timestamp: Long
    ) : AttributesOverrideLogRecordData(delegate, attributes, totalAttributeCount) {

        companion object {
            fun create(
                original: LogRecordData,
                creationElapsedTime: Long,
                timeOffsetNanos: Long
            ): TimeUpdatedLogRecordData {
                val attributes = Attributes.builder().putAll(original.attributes)
                    .remove(ATTRIBUTE_KEY_CREATION_ELAPSED_TIME)
                    .build()
                val realTimestamp = timeOffsetNanos + creationElapsedTime
                return TimeUpdatedLogRecordData(
                    original,
                    attributes,
                    original.totalAttributeCount - 1,
                    realTimestamp
                )
            }
        }

        override fun getTimestampEpochNanos(): Long {
            return timestamp
        }
    }

    private class TimeUpdatedSpanData private constructor(
        delegate: SpanData,
        attributes: Attributes,
        totalAttributeCount: Int,
        private val startEpochNanos: Long,
        private val endEpochNanos: Long
    ) : AttributesOverrideSpanData(delegate, attributes, totalAttributeCount) {

        companion object {
            fun create(
                original: SpanData,
                elapsedStartTime: Long,
                timeOffsetNanos: Long
            ): TimeUpdatedSpanData {
                val realStartTime = timeOffsetNanos + elapsedStartTime
                val realEndTime =
                    (original.endEpochNanos - original.startEpochNanos) + realStartTime
                val attributes = Attributes.builder().putAll(original.attributes)
                    .remove(ATTRIBUTE_KEY_CREATION_ELAPSED_TIME)
                    .build()
                return TimeUpdatedSpanData(
                    original,
                    attributes,
                    original.totalAttributeCount - 1,
                    realStartTime,
                    realEndTime
                )
            }
        }

        override fun getStartEpochNanos(): Long {
            return startEpochNanos
        }

        override fun getEndEpochNanos(): Long {
            return endEpochNanos
        }
    }
}