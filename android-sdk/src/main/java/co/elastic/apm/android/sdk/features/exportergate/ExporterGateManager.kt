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

import co.elastic.apm.android.sdk.exporters.configurable.MutableSpanExporter
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter

internal class ExporterGateManager(serviceManager: ServiceManager) {
    private val spanExporter by lazy { MutableSpanExporter() }
    private val spanGateQueue by lazy { ExporterGateQueue<SpanData>(1000, ::onSpanGateOpen) }
    private lateinit var delegateSpanExporter: SpanExporter
    private var gateSpanExporter: GateSpanExporter? = null
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }

    internal fun createSpanExporterGate(delegate: SpanExporter): SpanExporter {
        delegateSpanExporter = delegate
        gateSpanExporter = GateSpanExporter(delegateSpanExporter, spanGateQueue)
        spanExporter.setDelegate(gateSpanExporter)
        return spanExporter
    }

    internal fun createSpanGateLatch(): ExporterGateQueue.Latch {
        return spanGateQueue.createLatch()
    }

    internal fun setSpanQueueProcessingInterceptor(interceptor: Interceptor<SpanData>) {
        spanGateQueue.setQueueProcessingInterceptor(interceptor)
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
}