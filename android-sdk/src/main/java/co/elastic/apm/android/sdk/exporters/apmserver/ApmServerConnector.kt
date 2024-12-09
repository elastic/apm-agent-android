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
package co.elastic.apm.android.sdk.exporters.apmserver

import co.elastic.apm.android.sdk.exporters.configuration.ExportProtocol

class ApmServerConnector internal constructor(
    internal val connectivityConfigurationManager: ApmServerConnectivityConfigurationManager,
    internal val exporterProvider: ApmServerExporterProvider
) {
    fun setConnectivityConfiguration(configuration: ApmServerConnectivityConfiguration) {
        connectivityConfigurationManager.setConnectivityConfiguration(configuration)
    }

    fun getConnectivityConfiguration(): ApmServerConnectivityConfiguration {
        return connectivityConfigurationManager.getConnectivityConfiguration()
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    class Builder internal constructor() {
        private var url: String? = ""
        private var authentication: ApmServerConnectivityConfiguration.Auth =
            ApmServerConnectivityConfiguration.Auth.None
        private var exportProtocol: ExportProtocol = ExportProtocol.HTTP
        private val extraHeaders = mutableMapOf<String, String>()

        fun setUrl(value: String) = apply {
            url = value
        }

        fun setAuthentication(value: ApmServerConnectivityConfiguration.Auth) = apply {
            authentication = value
        }

        fun setExportProtocol(value: ExportProtocol) = apply {
            exportProtocol = value
        }

        fun addExtraHeader(value: Pair<String, String>) = apply {
            extraHeaders.plus(value)
        }

        fun build(): ApmServerConnector {
            url?.let { finalUrl ->
                val configuration =
                    ApmServerConnectivityConfiguration(
                        finalUrl,
                        authentication,
                        extraHeaders,
                        exportProtocol
                    )
                val configurationManager = ApmServerConnectivityConfigurationManager(configuration)
                val exporterProvider = ApmServerExporterProvider.create(configurationManager)
                return ApmServerConnector(configurationManager, exporterProvider)
            } ?: throw NullPointerException("The url must be set.")
        }
    }
}