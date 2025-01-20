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
package co.elastic.otel.android.internal.opentelemetry.processors.spans

import co.elastic.otel.android.interceptor.Interceptor
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.SpanProcessor

internal class SpanAttributesProcessor(private val interceptor: Interceptor<Attributes>) :
    SpanProcessor {

    override fun onStart(parentContext: Context, span: ReadWriteSpan) {
        span.setAllAttributes(interceptor.intercept(span.attributes))
    }

    override fun isStartRequired(): Boolean = true

    override fun onEnd(span: ReadableSpan) {

    }

    override fun isEndRequired(): Boolean = false
}