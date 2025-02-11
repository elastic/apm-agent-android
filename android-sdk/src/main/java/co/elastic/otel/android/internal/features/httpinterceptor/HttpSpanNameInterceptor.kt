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
package co.elastic.otel.android.internal.features.httpinterceptor

import co.elastic.otel.android.interceptor.Interceptor
import io.opentelemetry.sdk.trace.data.DelegatingSpanData
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.semconv.UrlAttributes

class HttpSpanNameInterceptor : Interceptor<SpanData> {
    private companion object {
        private val URL_PATTERN = Regex("https?://([^/]+).*")
    }

    override fun intercept(item: SpanData): SpanData {
        val url = item.attributes.get(UrlAttributes.URL_FULL) ?: return item

        return getNewName(item.name, url)?.let { newName ->
            NameDelegatingSpanData(item, newName)
        } ?: item
    }

    private fun getNewName(name: String, url: String): String? {
        return URL_PATTERN.matchEntire(url)?.let { match ->
            "$name ${match.groupValues[1]}"
        }
    }

    private class NameDelegatingSpanData(
        delegate: SpanData,
        private val name: String
    ) : DelegatingSpanData(delegate) {

        override fun getName(): String {
            return name
        }
    }
}