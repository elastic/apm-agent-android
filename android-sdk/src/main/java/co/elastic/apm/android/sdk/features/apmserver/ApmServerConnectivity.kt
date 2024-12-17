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
package co.elastic.apm.android.sdk.features.apmserver

import co.elastic.apm.android.sdk.connectivity.ConnectivityConfiguration
import co.elastic.apm.android.sdk.exporters.configuration.ExportProtocol

data class ApmServerConnectivity(
    private val url: String,
    val auth: ApmServerAuthentication = ApmServerAuthentication.None,
    val extraHeaders: Map<String, String> = emptyMap(),
    val exportProtocol: ExportProtocol = ExportProtocol.HTTP
) : ConnectivityConfiguration {
    private val baseUrl by lazy { url.trimEnd('/') }

    override fun getUrl(): String = url

    override fun getHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers.putAll(extraHeaders)
        if (auth is ApmServerAuthentication.SecretToken) {
            headers[AUTHORIZATION_HEADER_KEY] = "Bearer ${auth.token}"
        } else if (auth is ApmServerAuthentication.ApiKey) {
            headers[AUTHORIZATION_HEADER_KEY] = "ApiKey ${auth.key}"
        }
        return headers
    }

    fun getTracesUrl(): String {
        return getSignalUrl(baseUrl, "traces", exportProtocol)
    }

    fun getLogsUrl(): String {
        return getSignalUrl(baseUrl, "logs", exportProtocol)
    }

    fun getMetricsUrl(): String {
        return getSignalUrl(baseUrl, "metrics", exportProtocol)
    }

    private fun getSignalUrl(
        baseUrl: String,
        signalId: String,
        exportProtocol: ExportProtocol
    ): String {
        return when (exportProtocol) {
            ExportProtocol.GRPC -> baseUrl
            ExportProtocol.HTTP -> getHttpUrl(baseUrl, signalId)
        }
    }

    private fun getHttpUrl(url: String, signalId: String): String {
        return String.format("%s/v1/%s", url, signalId)
    }

    companion object {
        private const val AUTHORIZATION_HEADER_KEY = "Authorization"
    }
}
