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
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.semconv.ResourceAttributes

class ElasticAgent private constructor(val openTelemetry: OpenTelemetry) {

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(openTelemetry: OpenTelemetry): ElasticAgent {
            return ElasticAgent(openTelemetry)
        }
    }

    class Builder internal constructor() {
        private var serviceName: String = ""
        private var serviceVersion: String = ""
        private var deploymentEnvironment: String = ""
        private var clock: Clock = Clock.getDefault()
        private var spanProcessor: SpanProcessor? = null
        private var logRecordProcessor: LogRecordProcessor? = null
        private var metricReader: MetricReader? = null

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

        fun setSpanProcessor(value: SpanProcessor) = apply {
            spanProcessor = value
        }

        fun setLogRecordProcessor(value: LogRecordProcessor) = apply {
            logRecordProcessor = value
        }

        fun setMetricReader(value: MetricReader) = apply {
            metricReader = value
        }

        fun build(): ElasticAgent {
            val resource = Resource.builder()
                .put(ResourceAttributes.SERVICE_NAME, serviceName)
                .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, deploymentEnvironment)
                .build()
            val openTelemetryBuilder = OpenTelemetrySdk.builder()
            if (spanProcessor != null) {
                openTelemetryBuilder.setTracerProvider(
                    SdkTracerProvider.builder()
                        .setClock(clock)
                        .setResource(resource)
                        .addSpanProcessor(spanProcessor)
                        .build()
                )
            }
            if (logRecordProcessor != null) {
                openTelemetryBuilder.setLoggerProvider(
                    SdkLoggerProvider.builder()
                        .setClock(clock)
                        .setResource(resource)
                        .addLogRecordProcessor(logRecordProcessor)
                        .build()
                )
            }
            if (metricReader != null) {
                openTelemetryBuilder.setMeterProvider(
                    SdkMeterProvider.builder()
                        .setClock(clock)
                        .setResource(resource)
                        .registerMetricReader(metricReader)
                        .build()
                )
            }
            return create(openTelemetryBuilder.build())
        }
    }
}