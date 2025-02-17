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
package co.elastic.otel.android.internal.api

import android.app.Application
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.api.flusher.LogRecordFlusher
import co.elastic.otel.android.api.flusher.MetricFlusher
import co.elastic.otel.android.api.flusher.SpanFlusher
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.features.session.SessionIdGenerator
import co.elastic.otel.android.interceptor.Interceptor
import co.elastic.otel.android.internal.features.clock.ElasticClockManager
import co.elastic.otel.android.internal.features.conditionaldrop.ConditionalDropManager
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingManager
import co.elastic.otel.android.internal.features.exportergate.ExporterGateManager
import co.elastic.otel.android.internal.features.instrumentation.InstrumentationManager
import co.elastic.otel.android.internal.features.sessionmanager.SessionManager
import co.elastic.otel.android.internal.opentelemetry.ElasticOpenTelemetry
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.time.ntp.SntpClient
import co.elastic.otel.android.processors.ProcessorFactory
import co.elastic.otel.android.provider.StringProvider
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.UUID

class ManagedElasticOtelAgent private constructor(
    private val serviceManager: ServiceManager,
    internal val openTelemetry: ElasticOpenTelemetry,
    internal val features: ManagedFeatures
) : ElasticOtelAgent, MetricFlusher, LogRecordFlusher, SpanFlusher {

    init {
        features.elasticClockManager.initialize()
        features.diskBufferingManager.initialize()
        features.sessionManager.initialize()
        features.exporterGateManager.initialize()
        features.instrumentationManager.initialize(this)
    }

    override fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetry.sdk
    }

    override fun close() {
        features.diskBufferingManager.close()
        features.elasticClockManager.close()
        serviceManager.close()
        openTelemetry.sdk.close()
    }

    override fun flushMetrics(): CompletableResultCode {
        return openTelemetry.sdk.sdkMeterProvider.forceFlush()
    }

    override fun flushLogRecords(): CompletableResultCode {
        return openTelemetry.sdk.sdkLoggerProvider.forceFlush()
    }

    override fun flushSpans(): CompletableResultCode {
        return openTelemetry.sdk.sdkTracerProvider.forceFlush()
    }

    internal fun getElasticClockManager(): ElasticClockManager {
        return features.elasticClockManager
    }

    internal fun getExporterGateManager(): ExporterGateManager {
        return features.exporterGateManager
    }

    class ManagedFeatures private constructor(
        internal val exporterGateManager: ExporterGateManager,
        internal val diskBufferingManager: DiskBufferingManager,
        internal val elasticClockManager: ElasticClockManager,
        internal val sessionManager: SessionManager,
        internal val conditionalDropManager: ConditionalDropManager,
        internal val instrumentationManager: InstrumentationManager
    ) {
        class Builder(private val application: Application) {
            private var sessionIdGenerator: SessionIdGenerator? = null
            private var diskBufferingConfiguration = DiskBufferingConfiguration.enabled()
            private var sntpClient: SntpClient? = null
            private var gateSignalBufferSize = 1000

            fun setSessionIdGenerator(value: SessionIdGenerator) = apply {
                sessionIdGenerator = value
            }

            fun setDiskBufferingConfiguration(value: DiskBufferingConfiguration) = apply {
                diskBufferingConfiguration = value
            }

            fun setSntpClient(value: SntpClient) = apply {
                sntpClient = value
            }

            fun setGateSignalBufferSize(value: Int) {
                gateSignalBufferSize = value
            }

            fun build(
                serviceManager: ServiceManager,
                systemTimeProvider: SystemTimeProvider
            ): ManagedFeatures {
                val exporterGateManager = ExporterGateManager(
                    serviceManager,
                    signalBufferSize = gateSignalBufferSize
                )
                val diskBufferingManager = DiskBufferingManager.create(
                    systemTimeProvider,
                    serviceManager,
                    exporterGateManager,
                    diskBufferingConfiguration
                )
                val elasticClockManager = ElasticClockManager.create(
                    serviceManager,
                    exporterGateManager,
                    systemTimeProvider,
                    sntpClient ?: SntpClient.create(systemTimeProvider)
                )
                val sessionManager = SessionManager.create(
                    serviceManager,
                    sessionIdGenerator ?: SessionIdGenerator { UUID.randomUUID().toString() },
                    systemTimeProvider
                )
                val conditionalDropManager = ConditionalDropManager()
                val instrumentationManager = InstrumentationManager.create(application)

                return ManagedFeatures(
                    exporterGateManager,
                    diskBufferingManager,
                    elasticClockManager,
                    sessionManager,
                    conditionalDropManager,
                    instrumentationManager
                )
            }
        }
    }

    class Builder {
        private val elasticOpenTelemetryBuilder = ElasticOpenTelemetry.Builder()

        fun setServiceName(value: String) = apply {
            elasticOpenTelemetryBuilder.setServiceName(value)
        }

        fun setServiceVersion(value: String) = apply {
            elasticOpenTelemetryBuilder.setServiceVersion(value)
        }

        fun setServiceBuild(value: Int) = apply {
            elasticOpenTelemetryBuilder.setServiceBuild(value)
        }

        fun setDeploymentEnvironment(value: String) = apply {
            elasticOpenTelemetryBuilder.setDeploymentEnvironment(value)
        }

        fun setDeviceIdProvider(value: StringProvider) = apply {
            elasticOpenTelemetryBuilder.setDeviceIdProvider(value)
        }

        fun setResourceInterceptor(value: Interceptor<Resource>) = apply {
            elasticOpenTelemetryBuilder.setResourceInterceptor(value)
        }

        fun addSpanAttributesInterceptor(value: Interceptor<Attributes>) = apply {
            elasticOpenTelemetryBuilder.addSpanAttributesInterceptor(value)
        }

        fun addLogRecordAttributesInterceptor(value: Interceptor<Attributes>) = apply {
            elasticOpenTelemetryBuilder.addLogRecordAttributesInterceptor(value)
        }

        fun addSpanExporterInterceptor(value: Interceptor<SpanExporter>) = apply {
            elasticOpenTelemetryBuilder.addSpanExporterInterceptor(value)
        }

        fun addLogRecordExporterInterceptor(value: Interceptor<LogRecordExporter>) = apply {
            elasticOpenTelemetryBuilder.addLogRecordExporterInterceptor(value)
        }

        fun addMetricExporterInterceptor(value: Interceptor<MetricExporter>) = apply {
            elasticOpenTelemetryBuilder.addMetricExporterInterceptor(value)
        }

        fun setExporterProvider(value: ExporterProvider) = apply {
            elasticOpenTelemetryBuilder.setExporterProvider(value)
        }

        fun setProcessorFactory(value: ProcessorFactory) = apply {
            elasticOpenTelemetryBuilder.setProcessorFactory(value)
        }

        fun build(
            serviceManager: ServiceManager,
            features: ManagedFeatures
        ): ManagedElasticOtelAgent {
            elasticOpenTelemetryBuilder.addSpanAttributesInterceptor(
                features.elasticClockManager.getClockExportGateManager()
                    .getSpanAttributesInterceptor()
            )
            elasticOpenTelemetryBuilder.addLogRecordAttributesInterceptor(
                features.elasticClockManager.getClockExportGateManager()
                    .getLogRecordAttributesInterceptor()
            )
            addInternalInterceptors(
                features.diskBufferingManager,
                features.conditionalDropManager,
                features.elasticClockManager,
                features.exporterGateManager
            )
            elasticOpenTelemetryBuilder.setClock(features.elasticClockManager.getClock())
            elasticOpenTelemetryBuilder.setSessionProvider(features.sessionManager)

            return ManagedElasticOtelAgent(
                serviceManager,
                elasticOpenTelemetryBuilder.build(serviceManager),
                features
            )
        }

        private fun addInternalInterceptors(
            diskBufferingManager: DiskBufferingManager,
            conditionalDropManager: ConditionalDropManager,
            elasticClockManager: ElasticClockManager,
            exporterGateManager: ExporterGateManager
        ) {
            addDiskBufferingInterceptors(diskBufferingManager)
            addConditionalDropInterceptors(conditionalDropManager)
            addClockExporterInterceptors(elasticClockManager)
            addExporterGateInterceptors(exporterGateManager)
        }

        private fun addDiskBufferingInterceptors(diskBufferingManager: DiskBufferingManager) {
            elasticOpenTelemetryBuilder.addSpanExporterInterceptor(diskBufferingManager::interceptSpanExporter)
            elasticOpenTelemetryBuilder.addLogRecordExporterInterceptor(diskBufferingManager::interceptLogRecordExporter)
            elasticOpenTelemetryBuilder.addMetricExporterInterceptor(diskBufferingManager::interceptMetricExporter)
        }

        private fun addConditionalDropInterceptors(conditionalDropManager: ConditionalDropManager) {
            elasticOpenTelemetryBuilder.addSpanExporterInterceptor {
                conditionalDropManager.createConditionalDropSpanExporter(it)
            }
            elasticOpenTelemetryBuilder.addLogRecordExporterInterceptor {
                conditionalDropManager.createConditionalDropLogRecordExporter(it)
            }
            elasticOpenTelemetryBuilder.addMetricExporterInterceptor {
                conditionalDropManager.createConditionalDropMetricExporter(it)
            }
        }

        private fun addClockExporterInterceptors(elasticClockManager: ElasticClockManager) {
            elasticOpenTelemetryBuilder.addSpanExporterInterceptor {
                elasticClockManager.getClockExportGateManager().createSpanExporterDelegator(it)
            }
            elasticOpenTelemetryBuilder.addLogRecordExporterInterceptor {
                elasticClockManager.getClockExportGateManager().createLogRecordExporterDelegator(it)
            }
        }

        private fun addExporterGateInterceptors(exporterGateManager: ExporterGateManager) {
            elasticOpenTelemetryBuilder.addSpanExporterInterceptor {
                exporterGateManager.createSpanExporterGate(it)
            }
            elasticOpenTelemetryBuilder.addLogRecordExporterInterceptor {
                exporterGateManager.createLogRecordExporterGate(it)
            }
            elasticOpenTelemetryBuilder.addMetricExporterInterceptor {
                exporterGateManager.createMetricExporterGate(it)
            }
        }
    }
}