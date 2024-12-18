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

import co.elastic.apm.android.sdk.features.exportgate.GateSpanExporter
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.tools.AttributesOverrideSpanData
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import co.elastic.apm.android.sdk.tools.interceptor.MutableInterceptor
import co.elastic.apm.android.sdk.tools.provider.Provider
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.trace.data.SpanData

class ExportGateManager private constructor(
    systemTimeProvider: SystemTimeProvider,
    private val timeOffsetNanosProvider: Provider<Long?>
) {
    private var latch: GateSpanExporter.Latch? = null
    private val globalAttributesInterceptor =
        MutableInterceptor(ElapsedTimeAttributeInterceptor(systemTimeProvider))

    internal fun getAttributesInterceptor(): Interceptor<Attributes> {
        return globalAttributesInterceptor
    }

    internal fun onRemoteClockSet() {
        latch?.open()
        latch = null
    }

    companion object {
        internal fun create(
            systemTimeProvider: SystemTimeProvider,
            timeOffsetNanosProvider: Provider<Long?>,
            latch: GateSpanExporter.Latch
        ): ExportGateManager {
            val manager = ExportGateManager(systemTimeProvider, timeOffsetNanosProvider)
            manager.latch = latch
            return manager
        }

        private val ATTRIBUTE_KEY_ELAPSED_START_TIME =
            AttributeKey.longKey("internal.elastic.elapsed_start_time")
    }

    inner class GateDelegatingInterceptor : Interceptor<SpanData> {

        override fun intercept(item: SpanData): SpanData {
            val elapsedStartTime = item.attributes.get(ATTRIBUTE_KEY_ELAPSED_START_TIME)
            if (elapsedStartTime != null) {
                timeOffsetNanosProvider.get()?.let {
                    return TimeUpdatedSpanData.create(item, elapsedStartTime, it)
                }
            }
            return item
        }
    }

    inner class ElapsedTimeAttributeInterceptor(private val systemTimeProvider: SystemTimeProvider) :
        Interceptor<Attributes> {

        override fun intercept(item: Attributes): Attributes {
            return Attributes.builder().putAll(item)
                .put(ATTRIBUTE_KEY_ELAPSED_START_TIME, systemTimeProvider.getElapsedRealTime())
                .build()
        }
    }

    private class TimeUpdatedSpanData(
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
                    .remove(ATTRIBUTE_KEY_ELAPSED_START_TIME)
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