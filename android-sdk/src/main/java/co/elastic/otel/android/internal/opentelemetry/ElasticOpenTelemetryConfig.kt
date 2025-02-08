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
package co.elastic.otel.android.internal.opentelemetry

import android.os.Build
import co.elastic.otel.android.BuildConfig
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.interceptor.Interceptor
import co.elastic.otel.android.internal.attributes.CommonAttributesInterceptor
import co.elastic.otel.android.internal.attributes.SpanAttributesInterceptor
import co.elastic.otel.android.internal.opentelemetry.processors.DefaultProcessorFactory
import co.elastic.otel.android.internal.opentelemetry.processors.logs.LogRecordAttributesProcessor
import co.elastic.otel.android.internal.opentelemetry.processors.spans.SpanAttributesProcessor
import co.elastic.otel.android.internal.opentelemetry.processors.spans.SpanInterceptorProcessor
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.utilities.cache.PreferencesCachedStringProvider
import co.elastic.otel.android.processors.ProcessorFactory
import co.elastic.otel.android.provider.StringProvider
import co.elastic.otel.android.session.SessionProvider
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.semconv.ServiceAttributes
import io.opentelemetry.semconv.TelemetryAttributes
import io.opentelemetry.semconv.incubating.DeploymentIncubatingAttributes
import io.opentelemetry.semconv.incubating.DeviceIncubatingAttributes
import io.opentelemetry.semconv.incubating.OsIncubatingAttributes
import io.opentelemetry.semconv.incubating.ProcessIncubatingAttributes
import java.util.UUID

class ElasticOpenTelemetryConfig private constructor(
    val sdk: OpenTelemetrySdk,
    val serviceName: String,
    val deploymentEnvironment: String?,
    val clock: Clock
) {

    internal class Builder {
        private var serviceName: String = "unknown"
        private var serviceVersion: String? = null
        private var serviceBuild: Int? = null
        private var deploymentEnvironment: String? = null
        private var deviceIdProvider: StringProvider? = null
        private var spanAttributesInterceptors = mutableListOf<Interceptor<Attributes>>()
        private var logRecordAttributesInterceptors = mutableListOf<Interceptor<Attributes>>()
        private var spanExporterInterceptors = mutableListOf<Interceptor<SpanExporter>>()
        private var logRecordExporterInterceptors = mutableListOf<Interceptor<LogRecordExporter>>()
        private var metricExporterInterceptors = mutableListOf<Interceptor<MetricExporter>>()
        private var processorFactory: ProcessorFactory? = null
        private var sessionProvider: SessionProvider = SessionProvider.getDefault()
        private var clock: Clock = Clock.getDefault()
        private var exporterProvider: ExporterProvider = ExporterProvider.noop()

        fun setServiceName(value: String) = apply {
            serviceName = value
        }

        fun setServiceVersion(value: String) = apply {
            serviceVersion = value
        }

        fun setServiceBuild(value: Int) = apply {
            serviceBuild = value
        }

        fun setDeploymentEnvironment(value: String) = apply {
            deploymentEnvironment = value
        }

        fun setDeviceIdProvider(value: StringProvider) = apply {
            deviceIdProvider = value
        }

        fun addSpanAttributesInterceptor(value: Interceptor<Attributes>) = apply {
            spanAttributesInterceptors.add(value)
        }

        fun addLogRecordAttributesInterceptor(value: Interceptor<Attributes>) = apply {
            logRecordAttributesInterceptors.add(value)
        }

        fun addSpanExporterInterceptor(value: Interceptor<SpanExporter>) = apply {
            spanExporterInterceptors.add(value)
        }

        fun addLogRecordExporterInterceptor(value: Interceptor<LogRecordExporter>) = apply {
            logRecordExporterInterceptors.add(value)
        }

        fun addMetricExporterInterceptor(value: Interceptor<MetricExporter>) = apply {
            metricExporterInterceptors.add(value)
        }

        fun setProcessorFactory(value: ProcessorFactory) = apply {
            processorFactory = value
        }

        fun setSessionProvider(value: SessionProvider) = apply {
            sessionProvider = value
        }

        fun setClock(value: Clock) = apply {
            clock = value
        }

        fun setExporterProvider(value: ExporterProvider) = apply {
            exporterProvider = value
        }

        fun build(serviceManager: ServiceManager): ElasticOpenTelemetryConfig {
            val commonAttributesInterceptor =
                CommonAttributesInterceptor(serviceManager, sessionProvider)
            addSpanAttributesInterceptor(commonAttributesInterceptor)
            addSpanAttributesInterceptor(SpanAttributesInterceptor(serviceManager))
            addLogRecordAttributesInterceptor(commonAttributesInterceptor)
            if (deviceIdProvider == null) {
                deviceIdProvider = PreferencesCachedStringProvider(
                    serviceManager,
                    "device_id"
                ) { UUID.randomUUID().toString() }
            }
            val finalProcessorFactory = processorFactory
                ?: DefaultProcessorFactory(serviceManager.getBackgroundWorkService())
            val resource = Resource.builder()
                .put(ServiceAttributes.SERVICE_NAME, serviceName)
                .put(
                    ServiceAttributes.SERVICE_VERSION,
                    serviceVersion ?: serviceManager.getAppInfoService().getVersionName()
                    ?: "unknown"
                )
                .put(
                    AttributeKey.longKey("service.build"),
                    serviceBuild ?: serviceManager.getAppInfoService().getVersionCode()
                )
                .put(DeploymentIncubatingAttributes.DEPLOYMENT_ENVIRONMENT, deploymentEnvironment)
                .put(DeviceIncubatingAttributes.DEVICE_ID, deviceIdProvider!!.get())
                .put(DeviceIncubatingAttributes.DEVICE_MODEL_IDENTIFIER, Build.MODEL)
                .put(DeviceIncubatingAttributes.DEVICE_MANUFACTURER, Build.MANUFACTURER)
                .put(OsIncubatingAttributes.OS_DESCRIPTION, getOsDescription())
                .put(OsIncubatingAttributes.OS_VERSION, Build.VERSION.RELEASE)
                .put(OsIncubatingAttributes.OS_NAME, "Android")
                .put(ProcessIncubatingAttributes.PROCESS_RUNTIME_NAME, "Android Runtime")
                .put(
                    ProcessIncubatingAttributes.PROCESS_RUNTIME_VERSION,
                    System.getProperty("java.vm.version")
                )
                .put(TelemetryAttributes.TELEMETRY_SDK_NAME, "android")
                .put(TelemetryAttributes.TELEMETRY_SDK_VERSION, BuildConfig.APM_AGENT_VERSION)
                .put(TelemetryAttributes.TELEMETRY_SDK_LANGUAGE, "java")
                .build()
            val openTelemetryBuilder = OpenTelemetrySdk.builder()
            val spanExporter = exporterProvider.getSpanExporter()?.let {
                Interceptor.composite(spanExporterInterceptors).intercept(it)
            }
            val logRecordExporter = exporterProvider.getLogRecordExporter()?.let {
                Interceptor.composite(logRecordExporterInterceptors).intercept(it)
            }
            val metricExporter = exporterProvider.getMetricExporter()?.let {
                Interceptor.composite(metricExporterInterceptors).intercept(it)
            }
            finalProcessorFactory.createSpanProcessor(spanExporter)?.let {
                openTelemetryBuilder.setTracerProvider(
                    SdkTracerProvider.builder()
                        .setClock(clock)
                        .setResource(resource)
                        .addSpanProcessor(
                            SpanAttributesProcessor(
                                Interceptor.composite(
                                    spanAttributesInterceptors
                                )
                            )
                        )
                        .addSpanProcessor(SpanInterceptorProcessor())
                        .addSpanProcessor(it)
                        .build()
                )
            }
            finalProcessorFactory.createLogRecordProcessor(logRecordExporter)
                ?.let {
                    openTelemetryBuilder.setLoggerProvider(
                        SdkLoggerProvider.builder()
                            .setClock(clock)
                            .setResource(resource)
                            .addLogRecordProcessor(
                                LogRecordAttributesProcessor(
                                    Interceptor.composite(
                                        logRecordAttributesInterceptors
                                    )
                                )
                            )
                            .addLogRecordProcessor(it)
                            .build()
                    )
                }
            finalProcessorFactory.createMetricReader(metricExporter)?.let {
                openTelemetryBuilder.setMeterProvider(
                    SdkMeterProvider.builder()
                        .setClock(clock)
                        .setResource(resource)
                        .registerMetricReader(it)
                        .build()
                )
            }
            return ElasticOpenTelemetryConfig(
                openTelemetryBuilder.build(),
                serviceName,
                deploymentEnvironment,
                clock
            )
        }

        private fun getOsDescription(): String {
            val descriptionBuilder = StringBuilder()
            descriptionBuilder.append("Android ")
            descriptionBuilder.append(Build.VERSION.RELEASE)
            descriptionBuilder.append(", API level ")
            descriptionBuilder.append(Build.VERSION.SDK_INT)
            descriptionBuilder.append(", BUILD ")
            descriptionBuilder.append(Build.VERSION.INCREMENTAL)
            return descriptionBuilder.toString()
        }
    }
}