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
import co.elastic.apm.android.sdk.internal.api.ElasticOtelAgent
import co.elastic.apm.android.sdk.internal.opentelemetry.ElasticOpenTelemetryBuilder
import io.opentelemetry.api.OpenTelemetry

class ElasticAgent private constructor(
    configuration: Configuration,
    private val apmServerConnectivityManager: ApmServerConnectivityManager,
    private val centralConfigurationManager: CentralConfigurationManager
) : ElasticOtelAgent(configuration) {
    private val openTelemetry = configuration.openTelemetrySdk

    override fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetry
    }

    fun getApmServerConnectivityManager(): ApmServerConnectivityManager {
        return apmServerConnectivityManager
    }

    override fun onClose() {

    }

    companion object {
        @JvmStatic
        fun builder(application: Application): Builder {
            return Builder(application)
        }
    }

    class Builder internal constructor(application: Application) :
        ElasticOpenTelemetryBuilder<Builder>(application) {
        private var url: String? = null
        private var authentication: ApmServerAuthentication = ApmServerAuthentication.None
        private var exportProtocol: ExportProtocol = ExportProtocol.HTTP
        private var extraRequestHeaders: Map<String, String> = emptyMap()

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

        fun build(): ElasticAgent {
            url?.let { finalUrl ->
                val configuration =
                    ApmServerConnectivity(
                        finalUrl,
                        authentication,
                        extraRequestHeaders,
                        exportProtocol
                    )
                val configurationManager =
                    ApmServerConnectivityManager.ConfigurationManager(configuration)
                val apmServerConnectivityManager =
                    ApmServerConnectivityManager(configurationManager)
                val exporterProvider = ApmServerExporterProvider.create(configurationManager)
                setExporterProvider(exporterProvider)
                val elasticOtelConfig = buildConfiguration()
                val centralConfigurationManager = CentralConfigurationManager.create(
                    elasticOtelConfig.serviceManager,
                    serviceName,
                    deploymentEnvironment,
                    configurationManager
                )
                return ElasticAgent(
                    elasticOtelConfig,
                    apmServerConnectivityManager,
                    centralConfigurationManager
                )
            } ?: throw NullPointerException("The url must be set.")
        }
    }
}