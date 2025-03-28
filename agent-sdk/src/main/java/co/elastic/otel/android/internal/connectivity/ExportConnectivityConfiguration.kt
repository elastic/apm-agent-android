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
package co.elastic.otel.android.internal.connectivity

import co.elastic.otel.android.connectivity.Authentication
import co.elastic.otel.android.exporters.configuration.ExportProtocol
import co.elastic.otel.android.interceptor.Interceptor

internal class ExportConnectivityConfiguration(
    private val url: String,
    private val auth: Authentication,
    private val protocol: ExportProtocol,
    private val headersInterceptor: Interceptor<Map<String, String>> = Interceptor.noop()
) : ConnectivityConfiguration {
    private val baseUrl by lazy { url.trimEnd('/') }

    override fun getUrl(): String = url

    override fun getHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        if (auth is Authentication.SecretToken) {
            headers[AUTHORIZATION_HEADER_KEY] = "Bearer ${auth.token}"
        } else if (auth is Authentication.ApiKey) {
            headers[AUTHORIZATION_HEADER_KEY] = "ApiKey ${auth.key}"
        }
        return headersInterceptor.intercept(headers)
    }

    fun getProtocol(): ExportProtocol {
        return protocol
    }

    companion object {
        private const val AUTHORIZATION_HEADER_KEY = "Authorization"
    }
}
