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
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.common.CompletableResultCode
import java.util.UUID

class ManagedElasticOtelAgent private constructor(
    private val serviceManager: ServiceManager,
    private val configuration: Configuration,
    private val openTelemetryConfig: ElasticOpenTelemetryConfig
) : ElasticOtelAgent, MetricFlusher, LogRecordFlusher {

    init {
        configuration.elasticClockManager.initialize()
        configuration.diskBufferingManager.initialize()
        configuration.sessionManager.initialize()
        configuration.exporterGateManager.initialize()
        configuration.instrumentationManager.initialize(this)
    }

    override fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetryConfig.sdk
    }

    override fun close() {
        configuration.diskBufferingManager.close()
        configuration.elasticClockManager.close()
        serviceManager.close()
        openTelemetryConfig.sdk.close()
    }

    override fun flushMetrics(): CompletableResultCode {
        return openTelemetryConfig.sdk.sdkMeterProvider.forceFlush()
    }

    override fun flushLogRecords(): CompletableResultCode {
        return openTelemetryConfig.sdk.sdkLoggerProvider.forceFlush()
    }

    class Configuration private constructor(
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
            ): Configuration {
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

                return Configuration(
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

    internal class Builder(private val configuration: Configuration) {
        private val elasticOpenTelemetryConfigBuilder = ElasticOpenTelemetryConfig.Builder()

        fun setExporterProvider(value: ExporterProvider) = apply {
            elasticOpenTelemetryConfigBuilder.setExporterProvider(value)
        }

        fun build(serviceManager: ServiceManager): ManagedElasticOtelAgent {
            elasticOpenTelemetryConfigBuilder.addSpanAttributesInterceptor(
                configuration.elasticClockManager.getClockExportGateManager()
                    .getSpanAttributesInterceptor()
            )
            elasticOpenTelemetryConfigBuilder.addLogRecordAttributesInterceptor(
                configuration.elasticClockManager.getClockExportGateManager()
                    .getLogRecordAttributesInterceptor()
            )
            addInternalInterceptors(
                configuration.diskBufferingManager,
                configuration.conditionalDropManager,
                configuration.elasticClockManager,
                configuration.exporterGateManager
            )
            elasticOpenTelemetryConfigBuilder.setClock(configuration.elasticClockManager.getClock())
            elasticOpenTelemetryConfigBuilder.setSessionProvider(configuration.sessionManager)

            return ManagedElasticOtelAgent(
                serviceManager,
                configuration,
                elasticOpenTelemetryConfigBuilder.build(serviceManager)
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