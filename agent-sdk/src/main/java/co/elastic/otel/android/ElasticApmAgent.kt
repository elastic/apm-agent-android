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
import co.elastic.otel.android.connectivity.Authentication
import co.elastic.otel.android.connectivity.ExportEndpointConfiguration
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.exporters.configuration.ExportProtocol
import co.elastic.otel.android.features.session.SessionIdGenerator
import co.elastic.otel.android.interceptor.Interceptor
import co.elastic.otel.android.internal.api.ManagedElasticOtelAgent
import co.elastic.otel.android.internal.api.ManagedElasticOtelAgentContract
import co.elastic.otel.android.internal.connectivity.ExportConnectivityManager
import co.elastic.otel.android.internal.exporters.DefaultExporterProvider
import co.elastic.otel.android.internal.features.centralconfig.CentralConfigurationManager
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.features.exportergate.ExporterGateManager
import co.elastic.otel.android.internal.features.httpinterceptor.HttpSpanExporterInterceptor
import co.elastic.otel.android.internal.features.httpinterceptor.HttpSpanNameInterceptor
import co.elastic.otel.android.internal.features.sessionmanager.SessionManager
import co.elastic.otel.android.internal.features.sessionmanager.samplerate.SampleRateManager
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.time.ntp.SntpClient
import co.elastic.otel.android.internal.utilities.logging.AndroidLoggerFactory
import co.elastic.otel.android.logging.LogLevel
import co.elastic.otel.android.logging.LoggingPolicy
import co.elastic.otel.android.processors.ProcessorFactory
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter

/**
 * Opinionated [ElasticOtelAgent] implementation.
 */
@Suppress("CanBeParameter")
class ElasticApmAgent internal constructor(
    private val delegate: ManagedElasticOtelAgent,
    private val exportConnectivityManager: ExportConnectivityManager,
    private val centralConfigurationManager: CentralConfigurationManager?,
    private val sampleRateManager: SampleRateManager?
) : ManagedElasticOtelAgentContract {

    init {
        centralConfigurationManager?.initialize(delegate.openTelemetry)
        sampleRateManager?.initialize()
    }

    override fun getOpenTelemetry(): OpenTelemetry {
        return delegate.getOpenTelemetry()
    }

    override fun flushMetrics(): CompletableResultCode {
        return delegate.flushMetrics()
    }

    override fun flushLogRecords(): CompletableResultCode {
        return delegate.flushLogRecords()
    }

    override fun flushSpans(): CompletableResultCode {
        return delegate.flushSpans()
    }

    override fun close() {
        delegate.close()
    }

    /**
     * Updates the server connectivity parameters, this changes where the telemetry is sent.
     *
     * @param configuration The new server configuration.
     */
    fun setExportEndpointConfiguration(configuration: ExportEndpointConfiguration) {
        exportConnectivityManager.setEndpointConfiguration(configuration)
    }

    internal fun getExportConnectivityManager(): ExportConnectivityManager {
        return exportConnectivityManager
    }

    internal fun getExporterGateManager(): ExporterGateManager {
        return delegate.features.exporterGateManager
    }

    internal fun getCentralConfigurationManager(): CentralConfigurationManager? {
        return centralConfigurationManager
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

    /**
     * Builds an instance of [ElasticApmAgent].
     */
    class Builder internal constructor(private val application: Application) {
        private var exportUrl: String? = null
        private var exportAuthentication: Authentication = Authentication.None
        private var exportProtocol: ExportProtocol = ExportProtocol.HTTP
        private var exportHeadersInterceptor: Interceptor<Map<String, String>> = Interceptor.noop()
        private var managementUrl: String? = null
        private var managementAuthentication: Authentication = Authentication.None
        private var sessionIdGenerator: SessionIdGenerator? = null
        private var diskBufferingConfiguration: DiskBufferingConfiguration? = null
        private var loggingPolicy: LoggingPolicy? = null
        private var httpSpanInterceptor: Interceptor<SpanData>? = HttpSpanNameInterceptor()
        private val managedAgentBuilder = ManagedElasticOtelAgent.Builder()
        internal var internalExporterProviderInterceptor: Interceptor<ExporterProvider> =
            Interceptor.noop()
        internal var internalSntpClient: SntpClient? = null

        /**
         * This value is set as the [service.name](https://opentelemetry.io/docs/specs/semconv/resource/#service) resource attribute, which is later used as an
         * application identifier in Kibana, so it should typically contain the Android app name as value.
         *
         * Its default value is "unknown".
         */
        fun setServiceName(value: String) = apply {
            managedAgentBuilder.setServiceName(value)
        }

        /**
         * This value is set as the [service.version](https://opentelemetry.io/docs/specs/semconv/resource/#service) resource attribute,
         * which represents the Android app version.
         *
         * Its default value is the application version.
         */
        fun setServiceVersion(value: String) = apply {
            managedAgentBuilder.setServiceVersion(value)
        }

        /**
         * This value is set as the [deployment.environment](https://opentelemetry.io/docs/specs/semconv/attributes-registry/deployment/#deployment-environment) resource attribute.
         * It provides a place for Android applications to specify their environment/flavor or buildType.
         */
        fun setDeploymentEnvironment(value: String) = apply {
            managedAgentBuilder.setDeploymentEnvironment(value)
        }

        /**
         * This is the server URL where the telemetry is exported.
         */
        fun setExportUrl(value: String) = apply {
            exportUrl = value
        }

        /**
         * This is the authentication method needed to connect to the value provided in [setExportUrl].
         */
        fun setExportAuthentication(value: Authentication) = apply {
            exportAuthentication = value
        }

        /**
         * This is the protocol used to connect to the server provided in [setExportUrl], it can be either
         * HTTP or gRPC.
         *
         * Its default value is HTTP.
         */
        fun setExportProtocol(value: ExportProtocol) = apply {
            exportProtocol = value
        }

        /**
         * Allows to intercept the headers of the exporting requests.
         */
        fun setExportHeadersInterceptor(value: Interceptor<Map<String, String>>) = apply {
            exportHeadersInterceptor = value
        }

        /**
         * Allows to set an interceptor for attributes sent for each span.
         */
        fun addSpanAttributesInterceptor(value: Interceptor<Attributes>) = apply {
            managedAgentBuilder.addSpanAttributesInterceptor(value)
        }

        /**
         * Allows to set an interceptor for attributes sent for each log record.
         */
        fun addLogRecordAttributesInterceptor(value: Interceptor<Attributes>) = apply {
            managedAgentBuilder.addLogRecordAttributesInterceptor(value)
        }

        /**
         * Allows to set an interceptor for the agent-preconfigured [SpanExporter] during initialization.
         */
        fun addSpanExporterInterceptor(value: Interceptor<SpanExporter>) = apply {
            managedAgentBuilder.addSpanExporterInterceptor(value)
        }

        /**
         * Allows to set an interceptor for the agent-preconfigured [LogRecordExporter] during initialization.
         */
        fun addLogRecordExporterInterceptor(value: Interceptor<LogRecordExporter>) = apply {
            managedAgentBuilder.addLogRecordExporterInterceptor(value)
        }

        /**
         * Allows to set an interceptor for the agent-preconfigured [MetricExporter] during initialization.
         */
        fun addMetricExporterInterceptor(value: Interceptor<MetricExporter>) = apply {
            managedAgentBuilder.addMetricExporterInterceptor(value)
        }

        /**
         * Defines the agent's internal logging policy.
         *
         * By default it will be [LogLevel.DEBUG] for debuggable builds, [LogLevel.INFO] otherwise.
         */
        fun setInternalLoggingPolicy(value: LoggingPolicy) = apply {
            loggingPolicy = value
        }

        /**
         * It allows to set a custom [ProcessorFactory] where you can decide which [SpanProcessor], [LogRecordProcessor] and [MetricReader]
         * implementation you'd like to use.
         *
         * Its default values are the batch processors for spans and logs and the periodic metric reader for metrics.
         */
        fun setProcessorFactory(value: ProcessorFactory) = apply {
            managedAgentBuilder.setProcessorFactory(value)
        }

        /**
         * Allows to intercept the agent-preconfigured resources during initialization.
         */
        fun setResourceInterceptor(value: Interceptor<Resource>) = apply {
            managedAgentBuilder.setResourceInterceptor(value)
        }

        /**
         * Allows to intercept HTTP spans.
         */
        fun setHttpSpanInterceptor(value: Interceptor<SpanData>?) = apply {
            httpSpanInterceptor = value
        }

        internal fun setSessionIdGenerator(value: SessionIdGenerator) = apply {
            sessionIdGenerator = value
        }

        internal fun setManagementUrl(value: String) = apply {
            managementUrl = value
        }

        internal fun setManagementAuthentication(value: Authentication) = apply {
            managementAuthentication = value
        }

        internal fun setDiskBufferingConfiguration(value: DiskBufferingConfiguration) = apply {
            diskBufferingConfiguration = value
        }

        /**
         * Builds an [ElasticApmAgent] instance by using the provided configuration within this builder.
         */
        fun build(): ElasticApmAgent {
            val finalExportUrl =
                exportUrl ?: throw NullPointerException("The export URL must be set.")

            val serviceManager = ServiceManager.create(application)

            Elog.init(
                AndroidLoggerFactory(loggingPolicy ?: LoggingPolicy.getDefault(serviceManager))
            )

            val systemTimeProvider = SystemTimeProvider()
            val connectivityConfiguration = ExportEndpointConfiguration(
                finalExportUrl,
                exportAuthentication,
                exportProtocol
            )
            val exportConnectivityManager = ExportConnectivityManager.create(
                connectivityConfiguration,
                exportHeadersInterceptor
            )
            val exporterProvider = DefaultExporterProvider.create(exportConnectivityManager)

            val managedFeatures =
                createManagedConfiguration(serviceManager, systemTimeProvider)

            val centralConfigurationManager = configureCentralConfigurationManager(
                serviceManager,
                systemTimeProvider,
                managedFeatures
            )
            val sampleRateManager = configureSampleRateManager(
                serviceManager,
                centralConfigurationManager,
                managedFeatures
            )

            managedAgentBuilder.setExporterProvider(
                internalExporterProviderInterceptor.intercept(
                    exporterProvider
                )
            )

            httpSpanInterceptor?.let {
                managedAgentBuilder.addSpanExporterInterceptor(HttpSpanExporterInterceptor(it))
            }

            return ElasticApmAgent(
                managedAgentBuilder.build(serviceManager, managedFeatures),
                exportConnectivityManager,
                centralConfigurationManager,
                sampleRateManager
            )
        }

        private fun configureSampleRateManager(
            serviceManager: ServiceManager,
            centralConfigurationManager: CentralConfigurationManager?,
            managedFeatures: ManagedElasticOtelAgent.ManagedFeatures
        ): SampleRateManager? {
            return centralConfigurationManager?.let {
                val manager = SampleRateManager.create(
                    serviceManager,
                    managedFeatures.exporterGateManager,
                    it.getCentralConfiguration()
                )
                managedFeatures.sessionManager.addListener(manager)
                managedFeatures.conditionalDropManager.dropWhen {
                    !manager.allowSignalExporting()
                }
                manager
            }
        }

        private fun configureCentralConfigurationManager(
            serviceManager: ServiceManager,
            systemTimeProvider: SystemTimeProvider,
            managedFeatures: ManagedElasticOtelAgent.ManagedFeatures
        ): CentralConfigurationManager? {
            return managementUrl?.let {
                val manager = CentralConfigurationManager.create(
                    serviceManager,
                    CentralConfigurationManager.EndpointParameters(
                        it, managementAuthentication, emptyMap()
                    ),
                    systemTimeProvider,
                    managedFeatures.exporterGateManager
                )
                managedFeatures.conditionalDropManager.dropWhen {
                    !manager.getCentralConfiguration().isRecording()
                }
                manager
            }
        }

        private fun createManagedConfiguration(
            serviceManager: ServiceManager,
            systemTimeProvider: SystemTimeProvider
        ): ManagedElasticOtelAgent.ManagedFeatures {
            val managedConfigBuilder = ManagedElasticOtelAgent.ManagedFeatures.Builder(application)
            sessionIdGenerator?.let { managedConfigBuilder.setSessionIdGenerator(it) }
            diskBufferingConfiguration?.let { managedConfigBuilder.setDiskBufferingConfiguration(it) }
            internalSntpClient?.let { managedConfigBuilder.setSntpClient(it) }
            return managedConfigBuilder.build(serviceManager, systemTimeProvider)
        }
    }
}