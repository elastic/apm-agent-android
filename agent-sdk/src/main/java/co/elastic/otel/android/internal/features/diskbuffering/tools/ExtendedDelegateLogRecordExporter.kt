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
package co.elastic.otel.android.internal.features.diskbuffering.tools

import co.elastic.otel.android.internal.utilities.DelegateLogRecordData
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
// TODO - Remove after opentelemetry-java/issues/7363 is fixed.
class ExtendedDelegateLogRecordExporter(private val delegate: LogRecordExporter) :
    LogRecordExporter {

    override fun export(logs: MutableCollection<LogRecordData>): CompletableResultCode {
        return delegate.export(logs.map { DelegateLogRecordData(it) })
    }

    override fun flush(): CompletableResultCode {
        return delegate.flush()
    }

    override fun shutdown(): CompletableResultCode {
        return delegate.shutdown()
    }
}