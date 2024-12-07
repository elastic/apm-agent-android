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
package co.elastic.apm.android.sdk.internal.opentelemetry

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.BuildConfig
import co.elastic.apm.android.sdk.attributes.common.CommonAttributesInterceptor
import co.elastic.apm.android.sdk.attributes.common.SpanAttributesInterceptor
import co.elastic.apm.android.sdk.exporters.ExporterProvider
import co.elastic.apm.android.sdk.internal.opentelemetry.clock.ElasticClock
import co.elastic.apm.android.sdk.internal.opentelemetry.processors.logs.LogRecordAttributesProcessor
import co.elastic.apm.android.sdk.internal.opentelemetry.processors.spans.SpanAttributesProcessor
import co.elastic.apm.android.sdk.internal.opentelemetry.processors.spans.SpanInterceptorProcessor
import co.elastic.apm.android.sdk.processors.ProcessorFactory
import co.elastic.apm.android.sdk.session.SessionProvider
import co.elastic.apm.android.sdk.tools.Interceptor
import co.elastic.apm.android.sdk.tools.PreferencesCachedStringProvider
import co.elastic.apm.android.sdk.tools.provider.StringProvider
import io.opentelemetry.api.OpenTelemetry
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

@Suppress("UNCHECKED_CAST")
open class ElasticOpenTelemetryBuilder<B>(private val application: Application) {
    private var serviceName: String = ""
    private var serviceVersion: String = ""
    private var serviceBuild: Int? = null
    private var deploymentEnvironment: String = ""
    private var deviceIdProvider: StringProvider =
        PreferencesCachedStringProvider("device_id") { UUID.randomUUID().toString() }
    private var sessionProvider: SessionProvider = SessionProvider.getDefault()
    private var clock: Clock = ElasticClock.create()
    private var processorFactory: ProcessorFactory = ProcessorFactory.getDefault()
    private var spanAttributesInterceptors = mutableListOf<Interceptor<Attributes>>()
    private var logRecordAttributesInterceptors = mutableListOf<Interceptor<Attributes>>()
    private var spanExporterInterceptors = mutableListOf<Interceptor<SpanExporter>>()
    private var logRecordExporterInterceptors = mutableListOf<Interceptor<LogRecordExporter>>()
    private var metricExporterInterceptors = mutableListOf<Interceptor<MetricExporter>>()
    private var exporterProvider: ExporterProvider = ExporterProvider.noop()

    fun setServiceName(value: String): B {
        serviceName = value
        return this as B
    }

    fun setServiceVersion(value: String): B {
        serviceVersion = value
        return this as B
    }

    fun setServiceBuild(value: Int): B {
        serviceBuild = value
        return this as B
    }

    fun setDeploymentEnvironment(value: String): B {
        deploymentEnvironment = value
        return this as B
    }

    fun setDeviceIdProvider(value: StringProvider): B {
        deviceIdProvider = value
        return this as B
    }

    fun setSessionProvider(value: SessionProvider): B {
        sessionProvider = value
        return this as B
    }

    fun setClock(value: Clock): B {
        clock = value
        return this as B
    }

    fun setProcessorFactory(value: ProcessorFactory): B {
        processorFactory = value
        return this as B
    }

    fun addSpanAttributesInterceptor(value: Interceptor<Attributes>): B {
        spanAttributesInterceptors.add(value)
        return this as B
    }

    fun addLogRecordAttributesInterceptor(value: Interceptor<Attributes>): B {
        logRecordAttributesInterceptors.add(value)
        return this as B
    }

    fun addSpanExporterInterceptor(value: Interceptor<SpanExporter>): B {
        spanExporterInterceptors.add(value)
        return this as B
    }

    fun addLogRecordExporterInterceptor(value: Interceptor<LogRecordExporter>): B {
        logRecordExporterInterceptors.add(value)
        return this as B
    }

    fun addMetricExporterInterceptor(value: Interceptor<MetricExporter>): B {
        metricExporterInterceptors.add(value)
        return this as B
    }

    protected open fun setExporterProvider(value: ExporterProvider): B {
        exporterProvider = value
        return this as B
    }

    protected fun buildOpenTelemetry(): OpenTelemetry {
        val commonAttributesInterceptor = CommonAttributesInterceptor(sessionProvider)
        addSpanAttributesInterceptor(commonAttributesInterceptor)
        addSpanAttributesInterceptor(SpanAttributesInterceptor())
        addLogRecordAttributesInterceptor(commonAttributesInterceptor)
        val resource = Resource.builder()
            .put(ResourceAttributes.SERVICE_NAME, serviceName)
            .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
            .put(AttributeKey.longKey("service.build"), serviceBuild ?: getVersionCode())
            .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, deploymentEnvironment)
            .put(ResourceAttributes.DEVICE_ID, deviceIdProvider.get())
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
        val spanExporter = exporterProvider.getSpanExporter()?.also {
            Interceptor.composite(spanExporterInterceptors).intercept(it)
        }
        val logRecordExporter = exporterProvider.getLogRecordExporter()?.also {
            Interceptor.composite(logRecordExporterInterceptors).intercept(it)
        }
        val metricExporter = exporterProvider.getMetricExporter()?.also {
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
        return openTelemetryBuilder.build()
    }

    private fun getVersionCode(): Int {
        try {
            val info = application.packageManager.getPackageInfo(application.packageName, 0)
            return info.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Elog.getLogger().error("Getting version code", e)
            return 0
        }
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