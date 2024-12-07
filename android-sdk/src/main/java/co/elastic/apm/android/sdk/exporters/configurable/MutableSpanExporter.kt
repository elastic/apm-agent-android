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
package co.elastic.apm.android.sdk.exporters.configurable

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.atomic.AtomicReference

class MutableSpanExporter : SpanExporter {
    private val delegate = AtomicReference<SpanExporter?>()

    override fun export(spans: MutableCollection<SpanData>): CompletableResultCode {
        return delegate.get()?.export(spans) ?: CompletableResultCode.ofSuccess()
    }

    override fun flush(): CompletableResultCode {
        return delegate.get()?.flush() ?: CompletableResultCode.ofSuccess()
    }

    override fun shutdown(): CompletableResultCode {
        return delegate.get()?.shutdown() ?: CompletableResultCode.ofSuccess()
    }

    fun getDelegate(): SpanExporter? {
        return delegate.get()
    }

    fun setDelegate(value: SpanExporter?) {
        val old = delegate.get()
        delegate.set(value)
        if (value != old) {
            old?.shutdown()
        }
    }
}