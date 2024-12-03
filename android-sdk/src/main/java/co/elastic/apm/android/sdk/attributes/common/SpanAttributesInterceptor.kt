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

import co.elastic.apm.android.sdk.internal.services.Service
import co.elastic.apm.android.sdk.internal.services.ServiceManager
import co.elastic.apm.android.sdk.internal.services.network.NetworkService
import co.elastic.apm.android.sdk.session.SessionProvider
import co.elastic.apm.android.sdk.tools.Interceptor
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.semconv.SemanticAttributes

internal class SpanAttributesInterceptor(private val sessionProvider: SessionProvider) :
    Interceptor<Attributes> {
    private val networkService: NetworkService by lazy {
        ServiceManager.get().getService(Service.Names.NETWORK)
    }

    override fun intercept(item: Attributes): Attributes {
        val builder = Attributes.builder().putAll(item)
        val carrierInfo = networkService.getCarrierInfo()
        val networkType = networkService.type

        builder.put(SemanticAttributes.NETWORK_CONNECTION_TYPE, networkType.name)
        builder.put(SESSION_ID_ATTRIBUTE_KEY, sessionProvider.getSession().id)
        builder.put(TRANSACTION_TYPE_ATTRIBUTE_KEY, TRANSACTION_TYPE_VALUE)

        if (carrierInfo != null) {
            builder.put(SemanticAttributes.NETWORK_CARRIER_NAME, carrierInfo.name)
            builder.put(SemanticAttributes.NETWORK_CARRIER_MCC, carrierInfo.mcc)
            builder.put(SemanticAttributes.NETWORK_CARRIER_MNC, carrierInfo.mnc)
            builder.put(SemanticAttributes.NETWORK_CARRIER_ICC, carrierInfo.icc)
        }

        if (networkType.subTypeName != null) {
            builder.put(SemanticAttributes.NETWORK_CONNECTION_SUBTYPE, networkType.subTypeName)
        }
        return builder.build()
    }

    companion object {
        private val SESSION_ID_ATTRIBUTE_KEY: AttributeKey<String> =
            AttributeKey.stringKey("session.id")

        private val TRANSACTION_TYPE_ATTRIBUTE_KEY: AttributeKey<String> =
            AttributeKey.stringKey("type")

        private const val TRANSACTION_TYPE_VALUE: String = "mobile"
    }
}