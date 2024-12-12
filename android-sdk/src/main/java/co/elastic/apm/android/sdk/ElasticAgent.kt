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
import co.elastic.apm.android.sdk.exporters.configuration.ExportProtocol
import co.elastic.apm.android.sdk.features.apmserver.ApmServerAuthentication
import co.elastic.apm.android.sdk.features.apmserver.ApmServerConnectivity
import co.elastic.apm.android.sdk.features.apmserver.ApmServerConnectivityManager
import co.elastic.apm.android.sdk.features.apmserver.ApmServerExporterProvider
import co.elastic.apm.android.sdk.features.centralconfig.CentralConfigurationManager
import co.elastic.apm.android.sdk.features.clock.ElasticClockManager
import co.elastic.apm.android.sdk.features.sessionmanager.SessionIdGenerator
import co.elastic.apm.android.sdk.features.sessionmanager.SessionManager
import co.elastic.apm.android.sdk.internal.api.ElasticOtelAgent
import co.elastic.apm.android.sdk.internal.opentelemetry.ElasticOpenTelemetryBuilder
import co.elastic.apm.android.sdk.internal.opentelemetry.clock.ElasticClock
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import co.elastic.apm.android.sdk.tools.PreferencesLongCacheHandler
import co.elastic.apm.android.sdk.tools.PreferencesStringCacheHandler
import io.opentelemetry.api.OpenTelemetry
import java.util.UUID

class ElasticAgent private constructor(
    serviceManager: ServiceManager,
    configuration: Configuration,
    private val apmServerConnectivityManager: ApmServerConnectivityManager,
    private val elasticClockManager: ElasticClockManager,
    private val centralConfigurationManager: CentralConfigurationManager
) : ElasticOtelAgent(serviceManager, configuration) {
    private val openTelemetry = configuration.openTelemetrySdk

    init {
        elasticClockManager.initialize()
        centralConfigurationManager.initialize()
    }

    override fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetry
    }

    fun getApmServerConnectivityManager(): ApmServerConnectivityManager {
        return apmServerConnectivityManager
    }

    internal fun getCentralConfigurationManager(): CentralConfigurationManager {
        return centralConfigurationManager
    }

    override fun onClose() {
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
        internal var internalSntpClient: SntpClient? = null
        internal var internalSystemTimeProvider: SystemTimeProvider? = null

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

        fun build(): ElasticAgent {
            url?.let { finalUrl ->
                val serviceManager = ServiceManager.create(application)
                val apmServerConfiguration = ApmServerConnectivity(
                    finalUrl,
                    authentication,
                    extraRequestHeaders,
                    exportProtocol
                )
                val systemTimeProvider = internalSystemTimeProvider ?: SystemTimeProvider.get()
                val configurationManager =
                    ApmServerConnectivityManager.ConfigurationManager(apmServerConfiguration)
                val apmServerConnectivityManager =
                    ApmServerConnectivityManager(configurationManager)
                val exporterProvider = ApmServerExporterProvider.create(configurationManager)
                val clock = ElasticClock(
                    internalSntpClient ?: SntpClient.create(),
                    systemTimeProvider
                )
                val elasticClockManager = ElasticClockManager(
                    serviceManager,
                    clock
                )
                val centralConfigurationManager = CentralConfigurationManager.create(
                    serviceManager,
                    serviceName,
                    deploymentEnvironment,
                    configurationManager
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

                setClock(clock)
                setExporterProvider(exporterProvider)
                setSessionProvider(sessionManager)

                return ElasticAgent(
                    serviceManager,
                    buildConfiguration(serviceManager),
                    apmServerConnectivityManager,
                    elasticClockManager,
                    centralConfigurationManager
                )
            } ?: throw NullPointerException("The url must be set.")
        }
    }
}