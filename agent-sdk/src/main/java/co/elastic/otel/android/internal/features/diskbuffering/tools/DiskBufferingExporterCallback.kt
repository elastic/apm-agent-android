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

import co.elastic.otel.android.common.internal.logging.Elog
import io.opentelemetry.contrib.disk.buffering.exporters.callback.ExporterCallback
import io.opentelemetry.sdk.common.CompletableResultCode

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
class DiskBufferingExporterCallback<T>(
    private val signalId: String,
    private val networkExport: (MutableCollection<T>) -> CompletableResultCode
) : ExporterCallback<T> {
    private val logger = Elog.getLogger("disk_buffering_callback")

    override fun onExportSuccess(items: Collection<T>) {
        logger.debug("'$signalId' signals successfully stored in disk")
    }

    override fun onExportError(
        items: Collection<T>,
        error: Throwable?
    ) {
        logger.error(
            "'$signalId' signals failed to store in disk. Attempting to export right away.",
            error
        )
        networkExport.invoke(items as MutableCollection<T>)
    }

    override fun onShutdown() {
        logger.debug("'$signalId' signals disk buffer exporter closed")
    }
}