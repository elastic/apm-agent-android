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
package co.elastic.apm.android.sdk.features.exportergate

import co.elastic.apm.android.sdk.exporters.configurable.MutableLogRecordExporter
import co.elastic.apm.android.sdk.exporters.configurable.MutableMetricExporter
import co.elastic.apm.android.sdk.exporters.configurable.MutableSpanExporter
import co.elastic.apm.android.sdk.features.exportergate.latch.Latch
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class ExporterGateManager(
    serviceManager: ServiceManager,
    signalBufferSize: Int = 1000,
    private val gateLatchTimeout: Long = TimeUnit.SECONDS.toMillis(3),
    private val enableGateLatch: Boolean = true
) : ExporterGateQueue.Listener {
    private val spanExporter by lazy { MutableSpanExporter() }
    private val spanGateQueue by lazy {
        ExporterGateQueue<SpanData>(signalBufferSize, this, SPAN_QUEUE_ID, "Span")
    }
    private lateinit var delegateSpanExporter: SpanExporter
    private var gateSpanExporter: GateSpanExporter? = null
    private val logRecordExporter by lazy { MutableLogRecordExporter() }
    private val logRecordGateQueue by lazy {
        ExporterGateQueue<LogRecordData>(signalBufferSize, this, LOG_RECORD_QUEUE_ID, "Log")
    }
    private lateinit var delegateLogRecordExporter: LogRecordExporter
    private var gateLogRecordExporter: GateLogRecordExporter? = null
    private val metricExporter by lazy { MutableMetricExporter() }
    private val metricGateQueue by lazy {
        ExporterGateQueue<MetricData>(signalBufferSize, this, METRIC_QUEUE_ID, "Metric")
    }
    private lateinit var delegateMetricExporter: MetricExporter
    private var gateMetricExporter: GateMetricExporter? = null

    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }
    private val initializationLatch by lazy {
        Latch.composite(
            spanGateQueue.createLatch("Initialization"),
            logRecordGateQueue.createLatch("Initialization"),
            metricGateQueue.createLatch("Initialization")
        )
    }
    private val closedGates = AtomicInteger(3)

    companion object {
        private const val SPAN_QUEUE_ID = 1
        private const val LOG_RECORD_QUEUE_ID = 2
        private const val METRIC_QUEUE_ID = 3
    }

    internal fun initialize() {
        initializationLatch.open()
    }

    internal fun allGatesAreOpen(): Boolean {
        return closedGates.get() == 0
    }

    internal fun createSpanExporterGate(delegate: SpanExporter): SpanExporter {
        delegateSpanExporter = delegate
        gateSpanExporter = GateSpanExporter(delegateSpanExporter, spanGateQueue)
        spanExporter.setDelegate(gateSpanExporter)
        return spanExporter
    }

    internal fun createSpanGateLatch(name: String): Latch {
        if (!enableGateLatch) {
            return Latch.noop()
        }
        return spanGateQueue.createLatch(name)
    }

    internal fun setSpanQueueProcessingInterceptor(interceptor: Interceptor<SpanData>) {
        spanGateQueue.setQueueProcessingInterceptor(interceptor)
    }

    internal fun createLogRecordExporterGate(delegate: LogRecordExporter): LogRecordExporter {
        delegateLogRecordExporter = delegate
        gateLogRecordExporter = GateLogRecordExporter(delegateLogRecordExporter, logRecordGateQueue)
        logRecordExporter.setDelegate(gateLogRecordExporter)
        return logRecordExporter
    }

    internal fun createLogRecordLatch(name: String): Latch {
        if (!enableGateLatch) {
            return Latch.noop()
        }
        return logRecordGateQueue.createLatch(name)
    }

    internal fun setLogRecordQueueProcessingInterceptor(interceptor: Interceptor<LogRecordData>) {
        logRecordGateQueue.setQueueProcessingInterceptor(interceptor)
    }

    internal fun createMetricExporterGate(delegate: MetricExporter): MetricExporter {
        delegateMetricExporter = delegate
        gateMetricExporter = GateMetricExporter(delegateMetricExporter, metricGateQueue)
        metricExporter.setDelegate(gateMetricExporter)
        return metricExporter
    }

    internal fun createMetricGateLatch(name: String): Latch {
        if (!enableGateLatch) {
            return Latch.noop()
        }
        return metricGateQueue.createLatch(name)
    }

    internal fun setMetricQueueProcessingInterceptor(interceptor: Interceptor<MetricData>) {
        metricGateQueue.setQueueProcessingInterceptor(interceptor)
    }

    internal fun getAllOpenLatches(): List<Latch> {
        return spanGateQueue.getOpenLatches() + logRecordGateQueue.getOpenLatches() + metricGateQueue.getOpenLatches()
    }

    private fun onSpanGateOpen() {
        spanExporter.setDelegate(delegateSpanExporter)
        backgroundWorkService.submit {
            val processedItems = spanGateQueue.getProcessedItems()
            if (processedItems.isNotEmpty()) {
                delegateSpanExporter.export(processedItems)
            }
            gateSpanExporter = null
        }
        closedGates.decrementAndGet()
    }

    private fun onLogRecordGateOpen() {
        logRecordExporter.setDelegate(delegateLogRecordExporter)
        backgroundWorkService.submit {
            val processedItems = logRecordGateQueue.getProcessedItems()
            if (processedItems.isNotEmpty()) {
                delegateLogRecordExporter.export(processedItems)
            }
            gateLogRecordExporter = null
        }
        closedGates.decrementAndGet()
    }

    private fun onMetricGateOpen() {
        metricExporter.setDelegate(delegateMetricExporter)
        backgroundWorkService.submit {
            val processedItems = metricGateQueue.getProcessedItems()
            if (processedItems.isNotEmpty()) {
                delegateMetricExporter.export(processedItems)
            }
            gateMetricExporter = null
        }
        closedGates.decrementAndGet()
    }

    private fun onSpanQueueStarted() {
        backgroundWorkService.scheduleOnce(gateLatchTimeout) {
            spanGateQueue.forceOpenGate("Timeout")
        }
    }

    private fun onLogRecordQueueStarted() {
        backgroundWorkService.scheduleOnce(gateLatchTimeout) {
            logRecordGateQueue.forceOpenGate("Timeout")
        }
    }

    private fun onMetricQueueStarted() {
        backgroundWorkService.scheduleOnce(gateLatchTimeout) {
            metricGateQueue.forceOpenGate("Timeout")
        }
    }

    override fun onOpen(id: Int) {
        when (id) {
            SPAN_QUEUE_ID -> onSpanGateOpen()
            LOG_RECORD_QUEUE_ID -> onLogRecordGateOpen()
            METRIC_QUEUE_ID -> onMetricGateOpen()
            else -> throw IllegalArgumentException()
        }
    }

    override fun onStartEnqueuing(id: Int) {
        when (id) {
            SPAN_QUEUE_ID -> onSpanQueueStarted()
            LOG_RECORD_QUEUE_ID -> onLogRecordQueueStarted()
            METRIC_QUEUE_ID -> onMetricQueueStarted()
            else -> throw IllegalArgumentException()
        }
    }
}