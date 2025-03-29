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

import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.interceptor.Interceptor
import co.elastic.otel.android.internal.features.exportergate.ExporterGateManager
import co.elastic.otel.android.internal.provider.Provider
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.utilities.AttributesOverrideLogRecordData
import co.elastic.otel.android.internal.utilities.AttributesOverrideSpanData
import co.elastic.otel.android.internal.utilities.interceptor.MutableInterceptor
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class ClockExporterGateManager private constructor(
    systemTimeProvider: SystemTimeProvider,
    private val gateManager: ExporterGateManager,
    private val timeOffsetNanosProvider: Provider<Long?>
) {
    private val gateOpened = AtomicBoolean(false)
    private val spanAttrIntercepted = AtomicInteger(0)
    private val logRecordAttrIntercepted = AtomicInteger(0)
    private val spanAttributesInterceptor = MutableInterceptor(
        ElapsedTimeAttributeInterceptor(systemTimeProvider) {
            spanAttrIntercepted.incrementAndGet()
            logger.debug("Incrementing intercepted span attrs.")
        }
    )
    private val logRecordAttributesInterceptor = MutableInterceptor(
        ElapsedTimeAttributeInterceptor(systemTimeProvider) {
            logRecordAttrIntercepted.incrementAndGet()
            logger.debug("Incrementing intercepted log attrs.")
        }
    )
    private val logger = Elog.getLogger()

    internal fun getSpanAttributesInterceptor(): Interceptor<Attributes> {
        return spanAttributesInterceptor
    }

    internal fun getLogRecordAttributesInterceptor(): Interceptor<Attributes> {
        return logRecordAttributesInterceptor
    }

    internal fun createSpanExporterDelegator(delegate: SpanExporter): SpanExporter {
        return ClockSpanExporterDelegator(delegate)
    }

    internal fun createLogRecordExporterDelegator(delegate: LogRecordExporter): LogRecordExporter {
        return ClockLogRecordExporterDelegator(delegate)
    }

    internal fun onRemoteClockSet() {
        if (gateOpened.compareAndSet(false, true)) {
            gateManager.openLatches(ClockExporterGateManager::class.java)
            spanAttributesInterceptor.setDelegate(Interceptor.noop())
            logRecordAttributesInterceptor.setDelegate(Interceptor.noop())
            logger.debug("Remote clock set, clock gate manager cleared.")
        }
    }

    companion object {
        internal fun create(
            systemTimeProvider: SystemTimeProvider,
            gateManager: ExporterGateManager,
            timeOffsetNanosProvider: Provider<Long?>
        ): ClockExporterGateManager {
            gateManager.createSpanGateLatch(ClockExporterGateManager::class.java, "Clock")
            gateManager.createLogRecordLatch(ClockExporterGateManager::class.java, "Clock")
            return ClockExporterGateManager(
                systemTimeProvider,
                gateManager,
                timeOffsetNanosProvider
            )
        }

        private val ATTRIBUTE_KEY_CREATION_ELAPSED_TIME =
            AttributeKey.longKey("internal.elastic.creation_elapsed_time")
    }

    inner class ClockSpanExporterDelegator(private val delegate: SpanExporter) : SpanExporter {
        override fun export(spans: MutableCollection<SpanData>): CompletableResultCode {
            return delegate.export(intercept(spans))
        }

        override fun flush(): CompletableResultCode {
            return delegate.flush()
        }

        override fun shutdown(): CompletableResultCode {
            return delegate.shutdown()
        }

        private fun intercept(spans: Collection<SpanData>): Collection<SpanData> {
            if (spanAttrIntercepted.get() == 0) {
                logger.debug("[Clock] Skipping span attr intercept.")
                return spans
            }
            val intercepted = mutableListOf<SpanData>()
            for (span in spans) {
                intercepted.add(intercept(span))
            }
            return intercepted
        }

        private fun intercept(span: SpanData): SpanData {
            val elapsedStartTime = span.attributes.get(ATTRIBUTE_KEY_CREATION_ELAPSED_TIME)
            if (elapsedStartTime != null) {
                val updatedSpanData = TimeUpdatedSpanData.create(
                    span,
                    elapsedStartTime,
                    timeOffsetNanosProvider.get()
                )
                spanAttrIntercepted.decrementAndGet()
                logger.debug("[Clock] Decrementing intercepted span attrs.")
                return updatedSpanData
            }
            return span
        }
    }

    inner class ClockLogRecordExporterDelegator(private val delegate: LogRecordExporter) :
        LogRecordExporter {
        override fun export(logs: MutableCollection<LogRecordData>): CompletableResultCode {
            return delegate.export(intercept(logs))
        }

        override fun flush(): CompletableResultCode {
            return delegate.flush()
        }

        override fun shutdown(): CompletableResultCode {
            return delegate.shutdown()
        }

        private fun intercept(logs: Collection<LogRecordData>): Collection<LogRecordData> {
            if (logRecordAttrIntercepted.get() == 0) {
                logger.debug("[Clock] Skipping log attr intercept.")
                return logs
            }
            val intercepted = mutableListOf<LogRecordData>()
            for (log in logs) {
                intercepted.add(intercept(log))
            }
            return intercepted
        }

        private fun intercept(log: LogRecordData): LogRecordData {
            val creationElapsedTime = log.attributes.get(ATTRIBUTE_KEY_CREATION_ELAPSED_TIME)
            if (creationElapsedTime != null) {
                val updatedLogRecordData = TimeUpdatedLogRecordData.create(
                    log,
                    creationElapsedTime,
                    timeOffsetNanosProvider.get()
                )
                logRecordAttrIntercepted.decrementAndGet()
                logger.debug("[Clock] Decrementing intercepted log attrs.")
                return updatedLogRecordData
            }
            return log
        }
    }

    inner class ElapsedTimeAttributeInterceptor(
        private val systemTimeProvider: SystemTimeProvider,
        private val onIntercept: () -> Unit
    ) : Interceptor<Attributes> {

        override fun intercept(item: Attributes): Attributes {
            onIntercept.invoke()
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
                timeOffsetNanos: Long?
            ): TimeUpdatedLogRecordData {
                val attributes = Attributes.builder().putAll(original.attributes)
                    .remove(ATTRIBUTE_KEY_CREATION_ELAPSED_TIME)
                    .build()
                val timestamp = timeOffsetNanos?.let { it + creationElapsedTime }
                    ?: original.timestampEpochNanos
                return TimeUpdatedLogRecordData(
                    original,
                    attributes,
                    original.totalAttributeCount - 1,
                    timestamp
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
                timeOffsetNanos: Long?
            ): TimeUpdatedSpanData {
                val startTime =
                    timeOffsetNanos?.let { it + elapsedStartTime } ?: original.startEpochNanos
                val endTime =
                    timeOffsetNanos?.let { (original.endEpochNanos - original.startEpochNanos) + startTime }
                        ?: original.endEpochNanos
                val attributes = Attributes.builder().putAll(original.attributes)
                    .remove(ATTRIBUTE_KEY_CREATION_ELAPSED_TIME)
                    .build()
                return TimeUpdatedSpanData(
                    original,
                    attributes,
                    original.totalAttributeCount - 1,
                    startTime,
                    endTime
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