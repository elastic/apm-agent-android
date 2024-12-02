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
package co.elastic.apm.android.sdk

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.semconv.ResourceAttributes

class ElasticAgent internal constructor(
    val openTelemetry: OpenTelemetry,
    private val spanProcessor: SpanProcessor,
    private val logRecordProcessor: LogRecordProcessor,
    private val metricReader: MetricReader
) {

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    internal fun flushSpans(): CompletableResultCode {
        return spanProcessor.forceFlush()
    }

    internal fun flushMetrics(): CompletableResultCode {
        return metricReader.forceFlush()
    }

    internal fun flushLogRecords(): CompletableResultCode {
        return logRecordProcessor.forceFlush()
    }

    class Builder internal constructor() {
        private var serviceName: String = ""
        private var serviceVersion: String = ""
        private var deploymentEnvironment: String = ""
        private var spanExporter: SpanExporter? = null
        private var metricExporter: MetricExporter? = null
        private var logRecordExporter: LogRecordExporter? = null
        private var clock: Clock = Clock.getDefault()

        fun setServiceName(value: String) = apply {
            serviceName = value
        }

        fun setServiceVersion(value: String) = apply {
            serviceVersion = value
        }

        fun setDeploymentEnvironment(value: String) = apply {
            deploymentEnvironment = value
        }

        fun setClock(value: Clock) = apply {
            clock = value
        }

        fun setSpanExporter(value: SpanExporter) = apply {
            spanExporter = value
        }

        fun setMetricExporter(value: MetricExporter) = apply {
            metricExporter = value
        }

        fun setLogRecordExporter(value: LogRecordExporter) = apply {
            logRecordExporter = value
        }

        fun build(): ElasticAgent {
            val resource = Resource.builder()
                .put(ResourceAttributes.SERVICE_NAME, serviceName)
                .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, deploymentEnvironment)
                .build()
            val spanProcessor = BatchSpanProcessor.builder(spanExporter!!).build()
            val logRecordProcessor = BatchLogRecordProcessor.builder(logRecordExporter!!).build()
            val metricReader = PeriodicMetricReader.create(metricExporter!!)
            val openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(
                    SdkTracerProvider.builder()
                        .setClock(clock)
                        .setResource(resource)
                        .addSpanProcessor(spanProcessor)
                        .build()
                ).setLoggerProvider(
                    SdkLoggerProvider.builder()
                        .setClock(clock)
                        .setResource(resource)
                        .addLogRecordProcessor(
                            logRecordProcessor
                        )
                        .build()
                ).setMeterProvider(
                    SdkMeterProvider.builder()
                        .setClock(clock)
                        .setResource(resource)
                        .registerMetricReader(metricReader)
                        .build()
                )
                .build()
            return ElasticAgent(openTelemetry, spanProcessor, logRecordProcessor, metricReader)
        }
    }
}