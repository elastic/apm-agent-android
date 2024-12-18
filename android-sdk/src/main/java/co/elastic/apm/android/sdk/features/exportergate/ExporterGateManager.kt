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
import co.elastic.apm.android.sdk.exporters.configurable.MutableSpanExporter
import co.elastic.apm.android.sdk.features.exportergate.latch.Latch
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.TimeUnit

internal class ExporterGateManager(
    serviceManager: ServiceManager,
    signalBufferSize: Int = 1000,
    private val gateLatchTimeout: Long = TimeUnit.SECONDS.toMillis(3)
) : ExporterGateQueue.Listener {
    private val spanExporter by lazy { MutableSpanExporter() }
    private val spanGateQueue by lazy {
        ExporterGateQueue<SpanData>(signalBufferSize, this, SPAN_QUEUE_ID)
    }
    private lateinit var delegateSpanExporter: SpanExporter
    private var gateSpanExporter: GateSpanExporter? = null
    private val logRecordExporter by lazy { MutableLogRecordExporter() }
    private val logRecordGateQueue by lazy {
        ExporterGateQueue<LogRecordData>(signalBufferSize, this, LOG_RECORD_QUEUE_ID)
    }
    private lateinit var delegateLogRecordExporter: LogRecordExporter
    private var gateLogRecordExporter: GateLogRecordExporter? = null
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }

    companion object {
        private const val SPAN_QUEUE_ID = 1
        private const val LOG_RECORD_QUEUE_ID = 2
    }

    internal fun createSpanExporterGate(delegate: SpanExporter): SpanExporter {
        delegateSpanExporter = delegate
        gateSpanExporter = GateSpanExporter(delegateSpanExporter, spanGateQueue)
        spanExporter.setDelegate(gateSpanExporter)
        return spanExporter
    }

    internal fun createSpanGateLatch(): Latch {
        return spanGateQueue.createLatch()
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

    internal fun createLogRecordLatch(): Latch {
        return logRecordGateQueue.createLatch()
    }

    internal fun setLogRecordQueueProcessingInterceptor(interceptor: Interceptor<LogRecordData>) {
        logRecordGateQueue.setQueueProcessingInterceptor(interceptor)
    }

    private fun onSpanGateOpen() {
        spanExporter.setDelegate(delegateSpanExporter)
        if (spanGateQueue.hasAvailableItems()) {
            backgroundWorkService.submit {
                delegateSpanExporter.export(spanGateQueue.getProcessedItems())
                gateSpanExporter = null
            }
        }
    }

    private fun onLogRecordGateOpen() {
        logRecordExporter.setDelegate(delegateLogRecordExporter)
        if (logRecordGateQueue.hasAvailableItems()) {
            backgroundWorkService.submit {
                delegateLogRecordExporter.export(logRecordGateQueue.getProcessedItems())
                gateLogRecordExporter = null
            }
        }
    }

    private fun onSpanQueueStarted() {
        backgroundWorkService.schedule({
            spanGateQueue.openGate()
        }, gateLatchTimeout)
    }

    private fun onLogRecordQueueStarted() {
        backgroundWorkService.schedule({
            logRecordGateQueue.openGate()
        }, gateLatchTimeout)
    }

    override fun onOpen(id: Int) {
        when (id) {
            SPAN_QUEUE_ID -> onSpanGateOpen()
            LOG_RECORD_QUEUE_ID -> onLogRecordGateOpen()
            else -> throw IllegalArgumentException()
        }
    }

    override fun onStartEnqueuing(id: Int) {
        when (id) {
            SPAN_QUEUE_ID -> onSpanQueueStarted()
            LOG_RECORD_QUEUE_ID -> onLogRecordQueueStarted()
            else -> throw IllegalArgumentException()
        }
    }
}