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
package co.elastic.otel.android.internal.utilities

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import io.opentelemetry.sdk.logs.data.Body
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.resources.Resource

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal open class DelegateLogRecordData(private val delegate: LogRecordData) :
    LogRecordData {

    override fun getResource(): Resource {
        return delegate.resource
    }

    override fun getInstrumentationScopeInfo(): InstrumentationScopeInfo {
        return delegate.instrumentationScopeInfo
    }

    override fun getTimestampEpochNanos(): Long {
        return delegate.timestampEpochNanos
    }

    override fun getObservedTimestampEpochNanos(): Long {
        return delegate.observedTimestampEpochNanos
    }

    override fun getSpanContext(): SpanContext {
        return delegate.spanContext
    }

    override fun getSeverity(): Severity {
        return delegate.severity
    }

    override fun getSeverityText(): String? {
        return delegate.severityText
    }

    override fun getBody(): Body {
        return delegate.body
    }

    override fun getAttributes(): Attributes {
        return delegate.attributes
    }

    override fun getTotalAttributeCount(): Int {
        return delegate.totalAttributeCount
    }
}