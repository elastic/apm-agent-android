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
package co.elastic.otel.android.internal.features.exportergate

import co.elastic.otel.android.internal.exporters.configurable.MutableLogRecordExporter
import co.elastic.otel.android.internal.exporters.configurable.MutableMetricExporter
import co.elastic.otel.android.internal.exporters.configurable.MutableSpanExporter
import co.elastic.otel.android.internal.features.exportergate.latch.Latch
import co.elastic.otel.android.internal.services.ServiceManager
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class ExporterGateManager(
    serviceManager: ServiceManager,
    signalBufferSize: Int = 1000,
    private val gateLatchTimeout: Long = TimeUnit.SECONDS.toMillis(3)
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
    private val spanGateOpen = AtomicBoolean(false)
    private val logGateOpen = AtomicBoolean(false)
    private val metricGateOpen = AtomicBoolean(false)
    private var spanTimeoutTask: ScheduledFuture<*>? = null
    private var logTimeoutTask: ScheduledFuture<*>? = null
    private var metricTimeoutTask: ScheduledFuture<*>? = null

    companion object {
        private const val SPAN_QUEUE_ID = 1
        private const val LOG_RECORD_QUEUE_ID = 2
        private const val METRIC_QUEUE_ID = 3
    }

    init {
        spanGateQueue.createLatch(ExporterGateManager::class.java, "Initialization")
        logRecordGateQueue.createLatch(ExporterGateManager::class.java, "Initialization")
        metricGateQueue.createLatch(ExporterGateManager::class.java, "Initialization")
    }

    internal fun initialize() {
        openLatches(ExporterGateManager::class.java)
    }

    internal fun openLatches(holder: Class<*>) {
        spanGateQueue.openLatch(holder)
        logRecordGateQueue.openLatch(holder)
        metricGateQueue.openLatch(holder)
    }

    internal fun spanGateIsOpen(): Boolean {
        return spanGateOpen.get()
    }

    internal fun metricGateIsOpen(): Boolean {
        return metricGateOpen.get()
    }

    internal fun allGatesAreOpen(): Boolean {
        return spanGateOpen.get() && logGateOpen.get() && metricGateOpen.get()
    }

    internal fun createSpanExporterGate(delegate: SpanExporter): SpanExporter {
        delegateSpanExporter = delegate
        gateSpanExporter = GateSpanExporter(delegateSpanExporter, spanGateQueue)
        spanExporter.setDelegate(gateSpanExporter)
        return spanExporter
    }

    internal fun createSpanGateLatch(holder: Class<*>, name: String) {
        spanGateQueue.createLatch(holder, name)
    }

    internal fun createLogRecordExporterGate(delegate: LogRecordExporter): LogRecordExporter {
        delegateLogRecordExporter = delegate
        gateLogRecordExporter = GateLogRecordExporter(delegateLogRecordExporter, logRecordGateQueue)
        logRecordExporter.setDelegate(gateLogRecordExporter)
        return logRecordExporter
    }

    internal fun createLogRecordLatch(holder: Class<*>, name: String) {
        logRecordGateQueue.createLatch(holder, name)
    }

    internal fun createMetricExporterGate(delegate: MetricExporter): MetricExporter {
        delegateMetricExporter = delegate
        gateMetricExporter = GateMetricExporter(delegateMetricExporter, metricGateQueue)
        metricExporter.setDelegate(gateMetricExporter)
        return metricExporter
    }

    internal fun createMetricGateLatch(holder: Class<*>, name: String) {
        metricGateQueue.createLatch(holder, name)
    }

    internal fun getAllOpenLatches(): List<Latch> {
        return spanGateQueue.getOpenLatches() + logRecordGateQueue.getOpenLatches() + metricGateQueue.getOpenLatches()
    }

    private fun onSpanGateOpen() {
        spanTimeoutTask?.cancel(false)
        spanTimeoutTask = null
        spanExporter.setDelegate(delegateSpanExporter)
        backgroundWorkService.submit {
            val items = spanGateQueue.getEnqueuedItems()
            if (items.isNotEmpty()) {
                delegateSpanExporter.export(items)
            }
            gateSpanExporter = null
            spanGateOpen.set(true)
        }
    }

    private fun onLogRecordGateOpen() {
        logTimeoutTask?.cancel(false)
        logTimeoutTask = null
        logRecordExporter.setDelegate(delegateLogRecordExporter)
        backgroundWorkService.submit {
            val items = logRecordGateQueue.getEnqueuedItems()
            if (items.isNotEmpty()) {
                delegateLogRecordExporter.export(items)
            }
            gateLogRecordExporter = null
            logGateOpen.set(true)
        }
    }

    private fun onMetricGateOpen() {
        metricTimeoutTask?.cancel(false)
        metricTimeoutTask = null
        metricExporter.setDelegate(delegateMetricExporter)
        backgroundWorkService.submit {
            val items = metricGateQueue.getEnqueuedItems()
            if (items.isNotEmpty()) {
                delegateMetricExporter.export(items)
            }
            gateMetricExporter = null
            metricGateOpen.set(true)
        }
    }

    private fun onSpanQueueStarted() {
        spanTimeoutTask = backgroundWorkService.scheduleOnce(gateLatchTimeout) {
            spanGateQueue.forceOpenGate("Timeout")
        }
    }

    private fun onLogRecordQueueStarted() {
        logTimeoutTask = backgroundWorkService.scheduleOnce(gateLatchTimeout) {
            logRecordGateQueue.forceOpenGate("Timeout")
        }
    }

    private fun onMetricQueueStarted() {
        metricTimeoutTask = backgroundWorkService.scheduleOnce(gateLatchTimeout) {
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