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
package co.elastic.apm.android.sdk.attributes.common

import co.elastic.apm.android.sdk.internal.services.ServiceManager
import co.elastic.apm.android.sdk.internal.services.network.NetworkService
import co.elastic.apm.android.sdk.internal.services.network.data.NetworkType
import co.elastic.apm.android.sdk.session.SessionProvider
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.semconv.SemanticAttributes

internal class CommonAttributesInterceptor(
    private val serviceManager: ServiceManager,
    private val sessionProvider: SessionProvider
) : Interceptor<Attributes> {
    private val networkService: NetworkService by lazy {
        serviceManager.getNetworkService()
    }

    override fun intercept(item: Attributes): Attributes {
        val builder = Attributes.builder().putAll(item)
        val networkType = networkService.getType()

        builder.put(SemanticAttributes.NETWORK_CONNECTION_TYPE, networkType.name)
        sessionProvider.getSession()?.getId()?.let { builder.put(SESSION_ID_ATTRIBUTE_KEY, it) }

        if (networkType is NetworkType.Cell) {
            networkType.subTypeName?.let {
                builder.put(
                    SemanticAttributes.NETWORK_CONNECTION_SUBTYPE,
                    it
                )
            }
        }
        return builder.build()
    }

    companion object {
        private val SESSION_ID_ATTRIBUTE_KEY: AttributeKey<String> =
            AttributeKey.stringKey("session.id")
    }
}