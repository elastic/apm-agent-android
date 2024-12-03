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

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.attributes.common.SpanAttributesInterceptor
import co.elastic.apm.android.sdk.internal.opentelemetry.processors.logs.LogRecordAttributesProcessor
import co.elastic.apm.android.sdk.internal.opentelemetry.processors.spans.SpanAttributesProcessor
import co.elastic.apm.android.sdk.session.SessionProvider
import co.elastic.apm.android.sdk.tools.Interceptor
import co.elastic.apm.android.sdk.tools.PreferencesCachedStringProvider
import co.elastic.apm.android.sdk.tools.StringProvider
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
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
import java.util.UUID

class ElasticAgent private constructor(val openTelemetry: OpenTelemetry) {

    companion object {
        @JvmStatic
        fun builder(application: Application): Builder {
            return Builder(application)
        }

        @JvmStatic
        fun create(application: Application, openTelemetry: OpenTelemetry): ElasticAgent {
            return ElasticAgent(openTelemetry)
        }
    }

    class Builder internal constructor(private val application: Application) {
        private var serviceName: String = ""
        private var serviceVersion: String = ""
        private var serviceBuild: Int? = null
        private var deploymentEnvironment: String = ""
        private var deviceIdProvider: StringProvider =
            PreferencesCachedStringProvider("device_id") { UUID.randomUUID().toString() }
        private var sessionProvider: SessionProvider = SessionProvider.getDefault()
        private var clock: Clock = Clock.getDefault()
        private var spanProcessor: SpanProcessor? = null
        private var logRecordProcessor: LogRecordProcessor? = null
        private var metricReader: MetricReader? = null
        private var spanAttributesInterceptors = mutableListOf<Interceptor<Attributes>>()
        private var logRecordAttributesInterceptors = mutableListOf<Interceptor<Attributes>>()

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

        fun setSessionProvider(value: SessionProvider) = apply {
            sessionProvider = value
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

        fun addSpanAttributesInterceptor(value: Interceptor<Attributes>) = apply {
            spanAttributesInterceptors.add(value)
        }

        fun addLogRecordAttributesInterceptor(value: Interceptor<Attributes>) = apply {
            logRecordAttributesInterceptors.add(value)
        }

        fun build(): ElasticAgent {
            addSpanAttributesInterceptor(SpanAttributesInterceptor(sessionProvider))
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
            if (spanProcessor != null) {
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
                        .addSpanProcessor(spanProcessor)
                        .build()
                )
            }
            if (logRecordProcessor != null) {
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
            return create(application, openTelemetryBuilder.build())
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
}