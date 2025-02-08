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
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.features.session.SessionIdGenerator
import co.elastic.otel.android.internal.features.clock.ElasticClockManager
import co.elastic.otel.android.internal.features.conditionaldrop.ConditionalDropManager
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingManager
import co.elastic.otel.android.internal.features.exportergate.ExporterGateManager
import co.elastic.otel.android.internal.features.instrumentation.InstrumentationManager
import co.elastic.otel.android.internal.features.sessionmanager.SessionManager
import co.elastic.otel.android.internal.opentelemetry.ElasticOpenTelemetryConfig
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.time.ntp.SntpClient
import co.elastic.otel.android.processors.ProcessorFactory
import co.elastic.otel.android.provider.StringProvider
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.common.CompletableResultCode
import java.util.UUID

class ManagedElasticOtelAgent private constructor(
    private val serviceManager: ServiceManager,
    internal val openTelemetryConfig: ElasticOpenTelemetryConfig,
    internal val features: ManagedFeatures
) : ElasticOtelAgent, MetricFlusher, LogRecordFlusher {

    init {
        features.elasticClockManager.initialize()
        features.diskBufferingManager.initialize()
        features.sessionManager.initialize()
        features.exporterGateManager.initialize()
        features.instrumentationManager.initialize(this)
    }

    override fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetryConfig.sdk
    }

    override fun close() {
        features.diskBufferingManager.close()
        features.elasticClockManager.close()
        serviceManager.close()
        openTelemetryConfig.sdk.close()
    }

    override fun flushMetrics(): CompletableResultCode {
        return openTelemetryConfig.sdk.sdkMeterProvider.forceFlush()
    }

    override fun flushLogRecords(): CompletableResultCode {
        return openTelemetryConfig.sdk.sdkLoggerProvider.forceFlush()
    }

    class ManagedFeatures private constructor(
        internal val exporterGateManager: ExporterGateManager,
        internal val diskBufferingManager: DiskBufferingManager,
        internal val elasticClockManager: ElasticClockManager,
        internal val sessionManager: SessionManager,
        internal val conditionalDropManager: ConditionalDropManager,
        internal val instrumentationManager: InstrumentationManager
    ) {
        internal class Builder(private val application: Application) {
            private var sessionIdGenerator: SessionIdGenerator? = null
            private var diskBufferingConfiguration = DiskBufferingConfiguration.enabled()
            private var sntpClient: SntpClient? = null
            private var gateSignalBufferSize = 1000
            private var waitForClock = true

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

            fun setWaitForClock(value: Boolean) = apply {
                waitForClock = value
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
                    sntpClient ?: SntpClient.create(systemTimeProvider),
                    waitForClock
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

    internal class Builder {
        private val elasticOpenTelemetryConfigBuilder = ElasticOpenTelemetryConfig.Builder()
        private var internalClock: Clock? = null

        fun setServiceName(value: String) = apply {
            elasticOpenTelemetryConfigBuilder.setServiceName(value)
        }

        fun setServiceVersion(value: String) = apply {
            elasticOpenTelemetryConfigBuilder.setServiceVersion(value)
        }

        fun setDeploymentEnvironment(value: String) = apply {
            elasticOpenTelemetryConfigBuilder.setDeploymentEnvironment(value)
        }

        fun setDeviceIdProvider(value: StringProvider) = apply {
            elasticOpenTelemetryConfigBuilder.setDeviceIdProvider(value)
        }

        fun setExporterProvider(value: ExporterProvider) = apply {
            elasticOpenTelemetryConfigBuilder.setExporterProvider(value)
        }

        fun setProcessorFactory(value: ProcessorFactory) = apply {
            elasticOpenTelemetryConfigBuilder.setProcessorFactory(value)
        }

        internal fun setClock(value: Clock) = apply {
            internalClock = value
        }

        fun build(
            serviceManager: ServiceManager,
            features: ManagedFeatures
        ): ManagedElasticOtelAgent {
            elasticOpenTelemetryConfigBuilder.addSpanAttributesInterceptor(
                features.elasticClockManager.getClockExportGateManager()
                    .getSpanAttributesInterceptor()
            )
            elasticOpenTelemetryConfigBuilder.addLogRecordAttributesInterceptor(
                features.elasticClockManager.getClockExportGateManager()
                    .getLogRecordAttributesInterceptor()
            )
            addInternalInterceptors(
                features.diskBufferingManager,
                features.conditionalDropManager,
                features.elasticClockManager,
                features.exporterGateManager
            )
            internalClock?.let { elasticOpenTelemetryConfigBuilder.setClock(it) }
                ?: elasticOpenTelemetryConfigBuilder.setClock(features.elasticClockManager.getClock())
            elasticOpenTelemetryConfigBuilder.setSessionProvider(features.sessionManager)

            return ManagedElasticOtelAgent(
                serviceManager,
                elasticOpenTelemetryConfigBuilder.build(serviceManager),
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
            elasticOpenTelemetryConfigBuilder.addSpanExporterInterceptor(diskBufferingManager::interceptSpanExporter)
            elasticOpenTelemetryConfigBuilder.addLogRecordExporterInterceptor(diskBufferingManager::interceptLogRecordExporter)
            elasticOpenTelemetryConfigBuilder.addMetricExporterInterceptor(diskBufferingManager::interceptMetricExporter)
        }

        private fun addConditionalDropInterceptors(conditionalDropManager: ConditionalDropManager) {
            elasticOpenTelemetryConfigBuilder.addSpanExporterInterceptor {
                conditionalDropManager.createConditionalDropSpanExporter(it)
            }
            elasticOpenTelemetryConfigBuilder.addLogRecordExporterInterceptor {
                conditionalDropManager.createConditionalDropLogRecordExporter(it)
            }
            elasticOpenTelemetryConfigBuilder.addMetricExporterInterceptor {
                conditionalDropManager.createConditionalDropMetricExporter(it)
            }
        }

        private fun addClockExporterInterceptors(elasticClockManager: ElasticClockManager) {
            elasticOpenTelemetryConfigBuilder.addSpanExporterInterceptor {
                elasticClockManager.getClockExportGateManager().createSpanExporterDelegator(it)
            }
            elasticOpenTelemetryConfigBuilder.addLogRecordExporterInterceptor {
                elasticClockManager.getClockExportGateManager().createLogRecordExporterDelegator(it)
            }
        }

        private fun addExporterGateInterceptors(exporterGateManager: ExporterGateManager) {
            elasticOpenTelemetryConfigBuilder.addSpanExporterInterceptor {
                exporterGateManager.createSpanExporterGate(it)
            }
            elasticOpenTelemetryConfigBuilder.addLogRecordExporterInterceptor {
                exporterGateManager.createLogRecordExporterGate(it)
            }
            elasticOpenTelemetryConfigBuilder.addMetricExporterInterceptor {
                exporterGateManager.createMetricExporterGate(it)
            }
        }
    }
}