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
package co.elastic.otel.android.internal.features.centralconfig

import co.elastic.otel.android.connectivity.ConnectivityConfiguration
import co.elastic.otel.android.features.apmserver.ApmServerAuthentication

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal data class CentralConfigurationConnectivity(
    private val url: String,
    private val auth: ApmServerAuthentication,
    private val extraHeaders: Map<String, String>,
    val serviceName: String,
    val serviceDeployment: String?
) : ConnectivityConfiguration {
    private val baseUrl by lazy { url.trimEnd('/') + "/config/v1/agents?service.name=$serviceName" }

    override fun getUrl(): String {
        return when (serviceDeployment) {
            null -> baseUrl
            else -> "$baseUrl&service.deployment=$serviceDeployment"
        }
    }

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

    companion object {
        private const val AUTHORIZATION_HEADER_KEY = "Authorization"
    }
}