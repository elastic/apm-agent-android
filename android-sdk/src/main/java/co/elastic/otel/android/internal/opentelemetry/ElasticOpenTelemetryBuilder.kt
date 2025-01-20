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
import co.elastic.otel.android.internal.api.ManagedElasticOtelAgent
import co.elastic.otel.android.internal.attributes.CommonAttributesInterceptor
import co.elastic.otel.android.internal.attributes.SpanAttributesInterceptor
import co.elastic.otel.android.internal.opentelemetry.processors.logs.LogRecordAttributesProcessor
import co.elastic.otel.android.internal.opentelemetry.processors.spans.SpanAttributesProcessor
import co.elastic.otel.android.internal.opentelemetry.processors.spans.SpanInterceptorProcessor
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.utilities.cache.PreferencesCachedStringProvider
import co.elastic.otel.android.internal.utilities.interceptor.Interceptor
import co.elastic.otel.android.internal.utilities.provider.StringProvider
import co.elastic.otel.android.processors.ProcessorFactory
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
import io.opentelemetry.semconv.ResourceAttributes
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("UNCHECKED_CAST")
abstract class ElasticOpenTelemetryBuilder<B> {
    protected var serviceName: String = "unknown"
    protected var serviceVersion: String? = null
    protected var serviceBuild: Int? = null
    protected var deploymentEnvironment: String? = null
    private var deviceIdProvider: StringProvider? = null
    private var spanAttributesInterceptors = mutableListOf<Interceptor<Attributes>>()
    private var logRecordAttributesInterceptors = mutableListOf<Interceptor<Attributes>>()
    private var spanExporterInterceptors = mutableListOf<Interceptor<SpanExporter>>()
    private var logRecordExporterInterceptors = mutableListOf<Interceptor<LogRecordExporter>>()
    private var metricExporterInterceptors = mutableListOf<Interceptor<MetricExporter>>()
    private var processorFactory: ProcessorFactory = ProcessorFactory.getDefault()
    private var sessionProvider: SessionProvider = SessionProvider.getDefault()
    private var clock: Clock = Clock.getDefault()
    private var exporterProvider: ExporterProvider = ExporterProvider.noop()
    private val buildCalled = AtomicBoolean(false)

    fun setServiceName(value: String): B {
        checkNotBuilt()
        serviceName = value
        return this as B
    }

    fun setServiceVersion(value: String): B {
        checkNotBuilt()
        serviceVersion = value
        return this as B
    }

    fun setServiceBuild(value: Int): B {
        checkNotBuilt()
        serviceBuild = value
        return this as B
    }

    fun setDeploymentEnvironment(value: String): B {
        checkNotBuilt()
        deploymentEnvironment = value
        return this as B
    }

    fun setDeviceIdProvider(value: StringProvider): B {
        checkNotBuilt()
        deviceIdProvider = value
        return this as B
    }

    fun addSpanAttributesInterceptor(value: Interceptor<Attributes>): B {
        checkNotBuilt()
        spanAttributesInterceptors.add(value)
        return this as B
    }

    fun addLogRecordAttributesInterceptor(value: Interceptor<Attributes>): B {
        checkNotBuilt()
        logRecordAttributesInterceptors.add(value)
        return this as B
    }

    fun addSpanExporterInterceptor(value: Interceptor<SpanExporter>): B {
        checkNotBuilt()
        spanExporterInterceptors.add(value)
        return this as B
    }

    fun addLogRecordExporterInterceptor(value: Interceptor<LogRecordExporter>): B {
        checkNotBuilt()
        logRecordExporterInterceptors.add(value)
        return this as B
    }

    fun addMetricExporterInterceptor(value: Interceptor<MetricExporter>): B {
        checkNotBuilt()
        metricExporterInterceptors.add(value)
        return this as B
    }

    internal open fun setProcessorFactory(value: ProcessorFactory): B {
        checkNotBuilt()
        processorFactory = value
        return this as B
    }

    protected open fun setSessionProvider(value: SessionProvider): B {
        checkNotBuilt()
        sessionProvider = value
        return this as B
    }

    protected open fun setClock(value: Clock): B {
        checkNotBuilt()
        clock = value
        return this as B
    }

    protected open fun setExporterProvider(value: ExporterProvider): B {
        checkNotBuilt()
        exporterProvider = value
        return this as B
    }

    protected fun buildConfiguration(serviceManager: ServiceManager): ManagedElasticOtelAgent.Configuration {
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
        val resource = Resource.builder()
            .put(ResourceAttributes.SERVICE_NAME, serviceName)
            .put(
                ResourceAttributes.SERVICE_VERSION,
                serviceVersion ?: serviceManager.getAppInfoService().getVersionName() ?: "unknown"
            )
            .put(
                AttributeKey.longKey("service.build"),
                serviceBuild ?: serviceManager.getAppInfoService().getVersionCode()
            )
            .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, deploymentEnvironment)
            .put(ResourceAttributes.DEVICE_ID, deviceIdProvider!!.get())
            .put(ResourceAttributes.DEVICE_MODEL_IDENTIFIER, Build.MODEL)
            .put(ResourceAttributes.DEVICE_MANUFACTURER, Build.MANUFACTURER)
            .put(ResourceAttributes.OS_DESCRIPTION, getOsDescription())
            .put(ResourceAttributes.OS_VERSION, Build.VERSION.RELEASE)
            .put(ResourceAttributes.OS_NAME, "Android")
            .put(ResourceAttributes.PROCESS_RUNTIME_NAME, "Android Runtime")
            .put(
                ResourceAttributes.PROCESS_RUNTIME_VERSION,
                System.getProperty("java.vm.version")
            )
            .put(ResourceAttributes.TELEMETRY_SDK_NAME, "android")
            .put(ResourceAttributes.TELEMETRY_SDK_VERSION, BuildConfig.APM_AGENT_VERSION)
            .put(ResourceAttributes.TELEMETRY_SDK_LANGUAGE, "java")
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
        processorFactory.createSpanProcessor(spanExporter)?.let {
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
        processorFactory.createLogRecordProcessor(logRecordExporter)
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
        processorFactory.createMetricReader(metricExporter)?.let {
            openTelemetryBuilder.setMeterProvider(
                SdkMeterProvider.builder()
                    .setClock(clock)
                    .setResource(resource)
                    .registerMetricReader(it)
                    .build()
            )
        }
        buildCalled.set(true)
        return ManagedElasticOtelAgent.Configuration(openTelemetryBuilder.build())
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

    private fun checkNotBuilt() {
        if (buildCalled.get()) {
            throw IllegalStateException()
        }
    }
}