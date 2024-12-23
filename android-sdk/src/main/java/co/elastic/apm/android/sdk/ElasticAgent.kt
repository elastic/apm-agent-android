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
import co.elastic.apm.android.sdk.exporters.ExporterProvider
import co.elastic.apm.android.sdk.exporters.configuration.ExportProtocol
import co.elastic.apm.android.sdk.features.apmserver.ApmServerAuthentication
import co.elastic.apm.android.sdk.features.apmserver.ApmServerConnectivity
import co.elastic.apm.android.sdk.features.apmserver.ApmServerConnectivityManager
import co.elastic.apm.android.sdk.features.apmserver.ApmServerExporterProvider
import co.elastic.apm.android.sdk.features.centralconfig.CentralConfigurationManager
import co.elastic.apm.android.sdk.features.clock.ElasticClockManager
import co.elastic.apm.android.sdk.features.conditionaldrop.ConditionalDropManager
import co.elastic.apm.android.sdk.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.apm.android.sdk.features.diskbuffering.DiskBufferingManager
import co.elastic.apm.android.sdk.features.exportergate.ExporterGateManager
import co.elastic.apm.android.sdk.features.exportergate.latch.Latch
import co.elastic.apm.android.sdk.features.sessionmanager.SessionIdGenerator
import co.elastic.apm.android.sdk.features.sessionmanager.SessionManager
import co.elastic.apm.android.sdk.internal.api.ElasticOtelAgent
import co.elastic.apm.android.sdk.internal.opentelemetry.ElasticOpenTelemetryBuilder
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import co.elastic.apm.android.sdk.tools.PreferencesLongCacheHandler
import co.elastic.apm.android.sdk.tools.PreferencesStringCacheHandler
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import io.opentelemetry.api.OpenTelemetry
import java.util.UUID

class ElasticAgent private constructor(
    serviceManager: ServiceManager,
    configuration: Configuration,
    private val exporterGateManager: ExporterGateManager,
    private val diskBufferingManager: DiskBufferingManager,
    private val apmServerConnectivityManager: ApmServerConnectivityManager,
    private val elasticClockManager: ElasticClockManager,
    private val centralConfigurationManager: CentralConfigurationManager
) : ElasticOtelAgent(serviceManager, configuration) {
    private val openTelemetry = configuration.openTelemetrySdk

    init {
        elasticClockManager.initialize()
        diskBufferingManager.initialize()
        centralConfigurationManager.initialize()
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
        internal var internalSntpClient: SntpClient? = null
        internal var internalSystemTimeProvider: SystemTimeProvider? = null
        internal var internalExporterProviderInterceptor: Interceptor<ExporterProvider> =
            Interceptor.noop()
        internal var internalServiceManagerInterceptor: Interceptor<ServiceManager> =
            Interceptor.noop()
        internal var internalSignalBufferSize = 1000
        internal var internalEnableGateLatch = true

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

        internal fun setSessionIdGenerator(value: SessionIdGenerator) = apply {
            sessionIdGenerator = value
        }

        internal fun setDiskBufferingConfiguration(value: DiskBufferingConfiguration) = apply {
            diskBufferingConfiguration = value
        }

        fun build(): ElasticAgent {
            url?.let { finalUrl ->
                val serviceManager =
                    internalServiceManagerInterceptor.intercept(ServiceManager.create(application))
                val apmServerConfiguration = ApmServerConnectivity(
                    finalUrl,
                    authentication,
                    extraRequestHeaders,
                    exportProtocol
                )
                val systemTimeProvider = internalSystemTimeProvider ?: SystemTimeProvider.get()
                val connectivityHolder =
                    ApmServerConnectivityManager.ConnectivityHolder(apmServerConfiguration)
                val apmServerConnectivityManager =
                    ApmServerConnectivityManager(connectivityHolder)
                val exporterProvider = ApmServerExporterProvider.create(connectivityHolder)
                val exporterGateManager = ExporterGateManager(
                    serviceManager,
                    signalBufferSize = internalSignalBufferSize,
                    enableGateLatch = internalEnableGateLatch
                )
                val diskBufferingManager =
                    DiskBufferingManager(
                        serviceManager, diskBufferingConfiguration,
                        Latch.composite(
                            exporterGateManager.createSpanGateLatch(),
                            exporterGateManager.createLogRecordLatch(),
                            exporterGateManager.createMetricGateLatch()
                        )
                    )
                val elasticClockManager = ElasticClockManager.create(
                    serviceManager,
                    systemTimeProvider,
                    internalSntpClient ?: SntpClient.create(),
                    Latch.composite(
                        exporterGateManager.createSpanGateLatch(),
                        exporterGateManager.createLogRecordLatch()
                    )
                )
                val centralConfigurationManager = CentralConfigurationManager.create(
                    serviceManager,
                    systemTimeProvider,
                    serviceName,
                    deploymentEnvironment,
                    connectivityHolder,
                    Latch.composite(
                        exporterGateManager.createSpanGateLatch(),
                        exporterGateManager.createLogRecordLatch(),
                        exporterGateManager.createMetricGateLatch()
                    )
                )
                val sessionManager = SessionManager(
                    PreferencesStringCacheHandler(
                        "session_id",
                        serviceManager.getPreferencesService()
                    ),
                    PreferencesLongCacheHandler(
                        "session_id_expire_time",
                        serviceManager.getPreferencesService()
                    ),
                    PreferencesLongCacheHandler(
                        "session_id_next_time_for_update",
                        serviceManager.getPreferencesService()
                    ),
                    sessionIdGenerator ?: SessionIdGenerator { UUID.randomUUID().toString() },
                    systemTimeProvider
                )

                addSpanAttributesInterceptor(
                    elasticClockManager.getExportGateManager().getAttributesInterceptor()
                )
                addLogRecordAttributesInterceptor(
                    elasticClockManager.getExportGateManager().getAttributesInterceptor()
                )
                exporterGateManager.setSpanQueueProcessingInterceptor(
                    elasticClockManager.getExportGateManager()
                        .getSpanGateProcessingInterceptor()
                )
                exporterGateManager.setLogRecordQueueProcessingInterceptor(
                    elasticClockManager.getExportGateManager()
                        .getLogRecordGateProcessingInterceptor()
                )

                val conditionalDropManager = ConditionalDropManager()
                conditionalDropManager.dropWhen {
                    !centralConfigurationManager.getCentralConfiguration().isRecording()
                }

                addInternalInterceptors(
                    diskBufferingManager,
                    conditionalDropManager,
                    exporterGateManager
                )
                setClock(elasticClockManager.getClock())
                setExporterProvider(internalExporterProviderInterceptor.intercept(exporterProvider))
                setSessionProvider(sessionManager)

                return ElasticAgent(
                    serviceManager,
                    buildConfiguration(serviceManager),
                    exporterGateManager,
                    diskBufferingManager,
                    apmServerConnectivityManager,
                    elasticClockManager,
                    centralConfigurationManager
                )
            } ?: throw NullPointerException("The url must be set.")
        }

        private fun addInternalInterceptors(
            diskBufferingManager: DiskBufferingManager,
            conditionalDropManager: ConditionalDropManager,
            exporterGateManager: ExporterGateManager
        ) {
            addDiskBufferingInterceptors(diskBufferingManager)
            addConditionalDropInterceptors(conditionalDropManager)
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