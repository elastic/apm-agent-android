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
import co.elastic.otel.android.features.apmserver.ApmServerAuthentication
import co.elastic.otel.android.features.apmserver.ApmServerConnectivity
import co.elastic.otel.android.features.session.SessionIdGenerator
import co.elastic.otel.android.interceptor.Interceptor
import co.elastic.otel.android.internal.api.ManagedElasticOtelAgent
import co.elastic.otel.android.internal.features.apmserver.ApmServerConnectivityManager
import co.elastic.otel.android.internal.features.apmserver.ApmServerExporterProvider
import co.elastic.otel.android.internal.features.centralconfig.CentralConfigurationConnectivity
import co.elastic.otel.android.internal.features.centralconfig.CentralConfigurationManager
import co.elastic.otel.android.internal.features.clock.ElasticClockManager
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingManager
import co.elastic.otel.android.internal.features.exportergate.ExporterGateManager
import co.elastic.otel.android.internal.features.sessionmanager.SessionManager
import co.elastic.otel.android.internal.features.sessionmanager.samplerate.SampleRateManager
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.time.ntp.SntpClient
import co.elastic.otel.android.internal.utilities.logging.AndroidLoggerFactory
import co.elastic.otel.android.logging.LoggingPolicy
import co.elastic.otel.android.processors.ProcessorFactory
import io.opentelemetry.api.OpenTelemetry

@Suppress("CanBeParameter")
class ElasticApmAgent private constructor(
    private val delegate: ManagedElasticOtelAgent,
    private val apmServerConnectivityManager: ApmServerConnectivityManager,
    private val centralConfigurationManager: CentralConfigurationManager,
    private val sampleRateManager: SampleRateManager
) : ElasticOtelAgent {

    init {
        centralConfigurationManager.initialize(delegate.openTelemetryConfig)
        sampleRateManager.initialize()
    }

    override fun getOpenTelemetry(): OpenTelemetry {
        return delegate.getOpenTelemetry()
    }

    override fun close() {
        delegate.close()
    }

    fun setApmServerConnectivity(connectivity: ApmServerConnectivity) {
        apmServerConnectivityManager.setConnectivityConfiguration(connectivity)
        val currentCentralConnectivityConfig =
            centralConfigurationManager.getConnectivityConfiguration()
        centralConfigurationManager.setConnectivityConfiguration(
            CentralConfigurationConnectivity.fromApmServerConfig(
                currentCentralConnectivityConfig.serviceName,
                currentCentralConnectivityConfig.serviceDeployment,
                connectivity
            )
        )
    }

    internal fun getApmServerConnectivityManager(): ApmServerConnectivityManager {
        return apmServerConnectivityManager
    }

    internal fun getExporterGateManager(): ExporterGateManager {
        return delegate.features.exporterGateManager
    }

    internal fun getDiskBufferingManager(): DiskBufferingManager {
        return delegate.features.diskBufferingManager
    }

    internal fun getCentralConfigurationManager(): CentralConfigurationManager {
        return centralConfigurationManager
    }

    internal fun getElasticClockManager(): ElasticClockManager {
        return delegate.features.elasticClockManager
    }

    internal fun getSessionManager(): SessionManager {
        return delegate.features.sessionManager
    }

    companion object {
        @JvmStatic
        fun builder(application: Application): Builder {
            return Builder(application)
        }
    }

    class Builder internal constructor(private val application: Application) {
        private var url: String? = null
        private var authentication: ApmServerAuthentication = ApmServerAuthentication.None
        private var exportProtocol: ExportProtocol = ExportProtocol.HTTP
        private var extraRequestHeaders: Map<String, String> = emptyMap()
        private var sessionIdGenerator: SessionIdGenerator? = null
        private var diskBufferingConfiguration: DiskBufferingConfiguration? = null
        private var loggingPolicy: LoggingPolicy? = null
        private val managedAgentBuilder = ManagedElasticOtelAgent.Builder()
        internal var internalSntpClient: SntpClient? = null
        internal var internalSystemTimeProvider: SystemTimeProvider? = null
        internal var internalExporterProviderInterceptor: Interceptor<ExporterProvider> =
            Interceptor.noop()
        internal var internalServiceManagerInterceptor: Interceptor<ServiceManager> =
            Interceptor.noop()
        internal var internalSignalBufferSize: Int? = null
        internal var internalWaitForClock: Boolean? = null

        fun setServiceName(value: String) = apply {
            managedAgentBuilder.setServiceName(value)
        }

        fun setDeploymentEnvironment(value: String) = apply {
            managedAgentBuilder.setDeploymentEnvironment(value)
        }

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

        fun setSessionIdGenerator(value: SessionIdGenerator) = apply {
            sessionIdGenerator = value
        }

        fun setProcessorFactory(value: ProcessorFactory) = apply {
            managedAgentBuilder.setProcessorFactory(value)
        }

        internal fun setDiskBufferingConfiguration(value: DiskBufferingConfiguration) = apply {
            diskBufferingConfiguration = value
        }

        fun build(): ElasticApmAgent {
            val finalUrl = url ?: throw NullPointerException("The url must be set.")

            val serviceManager =
                internalServiceManagerInterceptor.intercept(ServiceManager.create(application))

            Elog.init(
                AndroidLoggerFactory(loggingPolicy ?: LoggingPolicy.getDefault(serviceManager))
            )

            val apmServerConfiguration = ApmServerConnectivity(
                finalUrl,
                authentication,
                extraRequestHeaders,
                exportProtocol
            )
            val systemTimeProvider = internalSystemTimeProvider ?: SystemTimeProvider()
            val apmServerConnectivityHolder =
                ApmServerConnectivityManager.ConnectivityHolder(apmServerConfiguration)
            val apmServerConnectivityManager =
                ApmServerConnectivityManager(apmServerConnectivityHolder)
            val exporterProvider = ApmServerExporterProvider.create(apmServerConnectivityHolder)

            val managedFeatures =
                createManagedConfiguration(serviceManager, systemTimeProvider)

            val centralConfigurationManager = CentralConfigurationManager.create(
                serviceManager,
                systemTimeProvider,
                managedFeatures.exporterGateManager,
                apmServerConnectivityHolder
            )
            val sampleRateManager = SampleRateManager.create(
                serviceManager,
                managedFeatures.exporterGateManager,
                centralConfigurationManager.getCentralConfiguration()
            )
            managedFeatures.sessionManager.addListener(sampleRateManager)
            managedFeatures.conditionalDropManager.dropWhen {
                !centralConfigurationManager.getCentralConfiguration().isRecording()
            }
            managedFeatures.conditionalDropManager.dropWhen {
                !sampleRateManager.allowSignalExporting()
            }

            managedAgentBuilder.setExporterProvider(
                internalExporterProviderInterceptor.intercept(
                    exporterProvider
                )
            )
            return ElasticApmAgent(
                managedAgentBuilder.build(serviceManager, managedFeatures),
                apmServerConnectivityManager,
                centralConfigurationManager,
                sampleRateManager
            )
        }

        private fun createManagedConfiguration(
            serviceManager: ServiceManager,
            systemTimeProvider: SystemTimeProvider
        ): ManagedElasticOtelAgent.ManagedFeatures {
            val managedConfigBuilder = ManagedElasticOtelAgent.ManagedFeatures.Builder(application)
            sessionIdGenerator?.let { managedConfigBuilder.setSessionIdGenerator(it) }
            diskBufferingConfiguration?.let { managedConfigBuilder.setDiskBufferingConfiguration(it) }
            internalSntpClient?.let { managedConfigBuilder.setSntpClient(it) }
            internalSignalBufferSize?.let { managedConfigBuilder.setGateSignalBufferSize(it) }
            internalWaitForClock?.let { managedConfigBuilder.setWaitForClock(it) }
            return managedConfigBuilder.build(serviceManager, systemTimeProvider)
        }
    }
}