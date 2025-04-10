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
package co.elastic.otel.android.internal.exporters.configurable

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import java.util.concurrent.atomic.AtomicReference

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class MutableLogRecordExporter : LogRecordExporter {
    private val delegate = AtomicReference<LogRecordExporter?>()

    override fun export(logs: MutableCollection<LogRecordData>): CompletableResultCode {
        return delegate.get()?.export(logs) ?: CompletableResultCode.ofSuccess()
    }

    override fun flush(): CompletableResultCode {
        return delegate.get()?.flush() ?: CompletableResultCode.ofSuccess()
    }

    override fun shutdown(): CompletableResultCode {
        return delegate.get()?.shutdown() ?: CompletableResultCode.ofSuccess()
    }

    fun getDelegate(): LogRecordExporter? {
        return delegate.get()
    }

    fun setDelegate(value: LogRecordExporter?) {
        delegate.set(value)
    }
}