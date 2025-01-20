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
package co.elastic.otel.android

import android.app.Application
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.exporters.configuration.ExportProtocol
import co.elastic.otel.android.internal.features.apmserver.ApmServerAuthentication
import co.elastic.otel.android.internal.features.apmserver.ApmServerConnectivity
import co.elastic.otel.android.internal.features.apmserver.ApmServerConnectivityManager
import co.elastic.otel.android.internal.features.apmserver.ApmServerExporterProvider
import co.elastic.otel.android.internal.features.centralconfig.CentralConfigurationManager
import co.elastic.otel.android.internal.features.clock.ElasticClockManager
import co.elastic.otel.android.internal.features.conditionaldrop.ConditionalDropManager
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingManager
import co.elastic.otel.android.internal.features.exportergate.ExporterGateManager
import co.elastic.otel.android.internal.features.sessionmanager.SessionIdGenerator
import co.elastic.otel.android.internal.features.sessionmanager.SessionManager
import co.elastic.otel.android.internal.features.sessionmanager.samplerate.SampleRateManager
import co.elastic.otel.android.internal.opentelemetry.ElasticOpenTelemetryBuilder
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.time.ntp.SntpClient
import co.elastic.otel.android.internal.utilities.interceptor.Interceptor
import co.elastic.otel.android.internal.utilities.logging.AndroidLoggerFactory
import co.elastic.otel.android.logging.LoggingPolicy
import io.opentelemetry.api.OpenTelemetry
import java.util.UUID

class ElasticApmAgent private constructor(
    serviceManager: ServiceManager,
    configuration: Configuration,
    sampleRateManager: SampleRateManager,
    private val exporterGateManager: ExporterGateManager,
    private val diskBufferingManager: DiskBufferingManager,
    private val apmServerConnectivityManager: ApmServerConnectivityManager,
    private val elasticClockManager: ElasticClockManager,
    private val centralConfigurationManager: CentralConfigurationManager,
    private val sessionManager: SessionManager
) : ElasticOtelAgent(serviceManager, configuration) {
    private val openTelemetry = configuration.openTelemetrySdk

    init {
        elasticClockManager.initialize()
        diskBufferingManager.initialize()
        centralConfigurationManager.initialize()
        sampleRateManager.initialize()
        sessionManager.initialize()
        exporterGateManager.initialize()
    }

    override fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetry
    }

    fun getApmServerConnectivityManager(): ApmServerConnectivityManager {
        return apmServerConnectivityManager
    }

    internal fun getExporterGateManager(): ExporterGateManager {
        return exporterGateManager
    }

    internal fun getDiskBufferingManager(): DiskBufferingManager {
        return diskBufferingManager
    }

    internal fun getCentralConfigurationManager(): CentralConfigurationManager {
        return centralConfigurationManager
    }

    internal fun getElasticClockManager(): ElasticClockManager {
        return elasticClockManager
    }

    internal fun getSessionManager(): SessionManager {
        return sessionManager
    }

    override fun onClose() {
        diskBufferingManager.close()
        elasticClockManager.close()
    }

    companion object {
        @JvmStatic
        fun builder(application: Application): Builder {
            return Builder(application)
        }
    }

    class Builder internal constructor(private val application: Application) :
        ElasticOpenTelemetryBuilder<Builder>() {
        private var url: String? = null
        private var authentication: ApmServerAuthentication = ApmServerAuthentication.None
        private var exportProtocol: ExportProtocol = ExportProtocol.HTTP
        private var extraRequestHeaders: Map<String, String> = emptyMap()
        private var sessionIdGenerator: SessionIdGenerator? = null
        private var diskBufferingConfiguration = DiskBufferingConfiguration.enabled()
        private var loggingPolicy: LoggingPolicy? = null
        internal var internalSntpClient: SntpClient? = null
        internal var internalSystemTimeProvider: SystemTimeProvider? = null
        internal var internalExporterProviderInterceptor: Interceptor<ExporterProvider> =
            Interceptor.noop()
        internal var internalServiceManagerInterceptor: Interceptor<ServiceManager> =
            Interceptor.noop()
        internal var internalSignalBufferSize = 1000
        internal var internalWaitForClock = true

        fun setUrl(value: String) = apply {
            url = value
        }

        fun setAuthentication(value: ApmServerAuthentication) = apply {
            authentication = value
        }

        fun setExportProtocol(value: ExportProtocol) = apply {
            exportProtocol = value
        }

        fun setExtraRequestHeaders(value: Map<String, String>) = apply {
            extraRequestHeaders = value
        }

        fun setLoggingPolicy(value: LoggingPolicy) = apply {
            loggingPolicy = value
        }

        internal fun setSessionIdGenerator(value: SessionIdGenerator) = apply {
            sessionIdGenerator = value
        }

        internal fun setDiskBufferingConfiguration(value: DiskBufferingConfiguration) = apply {
            diskBufferingConfiguration = value
        }

        fun build(): ElasticApmAgent {
            val finalUrl = url ?: throw NullPointerException("The url must be set.")

            val serviceManager =
                internalServiceManagerInterceptor.intercept(ServiceManager.create(application))

            Elog.init(
                AndroidLoggerFactory(
                    loggingPolicy ?: LoggingPolicy.getDefault(serviceManager)
                )
            )

            val apmServerConfiguration = ApmServerConnectivity(
                finalUrl,
                authentication,
                extraRequestHeaders,
                exportProtocol
            )
            val systemTimeProvider = internalSystemTimeProvider ?: SystemTimeProvider()
            val connectivityHolder =
                ApmServerConnectivityManager.ConnectivityHolder(apmServerConfiguration)
            val apmServerConnectivityManager =
                ApmServerConnectivityManager(connectivityHolder)
            val exporterProvider = ApmServerExporterProvider.create(connectivityHolder)
            val exporterGateManager = ExporterGateManager(
                serviceManager,
                signalBufferSize = internalSignalBufferSize
            )
            val diskBufferingManager = DiskBufferingManager.create(
                systemTimeProvider, serviceManager, exporterGateManager, diskBufferingConfiguration
            )
            val elasticClockManager = ElasticClockManager.create(
                serviceManager,
                exporterGateManager,
                systemTimeProvider,
                internalSntpClient ?: SntpClient.create(systemTimeProvider),
                internalWaitForClock
            )
            val centralConfigurationManager = CentralConfigurationManager.create(
                serviceManager,
                systemTimeProvider,
                exporterGateManager,
                serviceName,
                deploymentEnvironment,
                connectivityHolder
            )
            val sessionManager = SessionManager.create(
                serviceManager,
                sessionIdGenerator ?: SessionIdGenerator { UUID.randomUUID().toString() },
                systemTimeProvider
            )
            val sampleRateManager = SampleRateManager.create(
                serviceManager,
                exporterGateManager,
                centralConfigurationManager.getCentralConfiguration()
            )
            sessionManager.addListener(sampleRateManager)

            addSpanAttributesInterceptor(
                elasticClockManager.getClockExportGateManager().getSpanAttributesInterceptor()
            )
            addLogRecordAttributesInterceptor(
                elasticClockManager.getClockExportGateManager()
                    .getLogRecordAttributesInterceptor()
            )

            val conditionalDropManager = ConditionalDropManager()
            conditionalDropManager.dropWhen {
                !centralConfigurationManager.getCentralConfiguration().isRecording()
            }
            conditionalDropManager.dropWhen {
                !sampleRateManager.allowSignalExporting()
            }

            addInternalInterceptors(
                diskBufferingManager,
                conditionalDropManager,
                elasticClockManager,
                exporterGateManager
            )
            setClock(elasticClockManager.getClock())
            setExporterProvider(internalExporterProviderInterceptor.intercept(exporterProvider))
            setSessionProvider(sessionManager)

            return ElasticApmAgent(
                serviceManager,
                buildConfiguration(serviceManager),
                sampleRateManager,
                exporterGateManager,
                diskBufferingManager,
                apmServerConnectivityManager,
                elasticClockManager,
                centralConfigurationManager,
                sessionManager
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
            addSpanExporterInterceptor(diskBufferingManager::interceptSpanExporter)
            addLogRecordExporterInterceptor(diskBufferingManager::interceptLogRecordExporter)
            addMetricExporterInterceptor(diskBufferingManager::interceptMetricExporter)
        }

        private fun addConditionalDropInterceptors(conditionalDropManager: ConditionalDropManager) {
            addSpanExporterInterceptor {
                conditionalDropManager.createConditionalDropSpanExporter(it)
            }
            addLogRecordExporterInterceptor {
                conditionalDropManager.createConditionalDropLogRecordExporter(it)
            }
            addMetricExporterInterceptor {
                conditionalDropManager.createConditionalDropMetricExporter(it)
            }
        }

        private fun addClockExporterInterceptors(elasticClockManager: ElasticClockManager) {
            addSpanExporterInterceptor {
                elasticClockManager.getClockExportGateManager().createSpanExporterDelegator(it)
            }
            addLogRecordExporterInterceptor {
                elasticClockManager.getClockExportGateManager().createLogRecordExporterDelegator(it)
            }
        }

        private fun addExporterGateInterceptors(exporterGateManager: ExporterGateManager) {
            addSpanExporterInterceptor {
                exporterGateManager.createSpanExporterGate(it)
            }
            addLogRecordExporterInterceptor {
                exporterGateManager.createLogRecordExporterGate(it)
            }
            addMetricExporterInterceptor {
                exporterGateManager.createMetricExporterGate(it)
            }
        }
    }
}