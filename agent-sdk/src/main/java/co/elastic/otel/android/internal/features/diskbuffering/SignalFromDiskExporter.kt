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
package co.elastic.otel.android.internal.features.diskbuffering

import androidx.annotation.WorkerThread
import co.elastic.otel.android.common.internal.logging.Elog
import io.opentelemetry.contrib.disk.buffering.LogRecordFromDiskExporter
import io.opentelemetry.contrib.disk.buffering.MetricFromDiskExporter
import io.opentelemetry.contrib.disk.buffering.SpanFromDiskExporter
import java.io.Closeable
import java.util.concurrent.TimeUnit

/**
 * Entrypoint to read and export previously cached signals.
 *
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class SignalFromDiskExporter internal constructor(
    private val spanDiskExporter: SpanFromDiskExporter?,
    private val metricDiskExporter: MetricFromDiskExporter?,
    private val logRecordDiskExporter: LogRecordFromDiskExporter?,
    private val exportTimeoutInMillis: Long
) : Closeable {
    private val logger = Elog.getLogger()

    @WorkerThread
    fun exportBatchOfSpans(): Boolean {
        return try {
            spanDiskExporter?.exportStoredBatch(exportTimeoutInMillis, TimeUnit.MILLISECONDS)
                ?: false
        } catch (t: Throwable) {
            logger.error("Error while trying to export spans from disk", t)
            false
        }
    }

    @WorkerThread
    fun exportBatchOfMetrics(): Boolean {
        return try {
            metricDiskExporter?.exportStoredBatch(exportTimeoutInMillis, TimeUnit.MILLISECONDS)
                ?: false
        } catch (t: Throwable) {
            logger.error("Error while trying to export metrics from disk", t)
            false
        }
    }

    @WorkerThread
    fun exportBatchOfLogs(): Boolean {
        return try {
            logRecordDiskExporter?.exportStoredBatch(
                exportTimeoutInMillis,
                TimeUnit.MILLISECONDS
            ) ?: false
        } catch (t: Throwable) {
            logger.error("Error while trying to export logs from disk", t)
            false
        }
    }

    @WorkerThread
    fun exportBatchOfEach(): Boolean {
        var atLeastOneWorked = exportBatchOfSpans()
        if (exportBatchOfMetrics()) {
            atLeastOneWorked = true
        }
        if (exportBatchOfLogs()) {
            atLeastOneWorked = true
        }
        return atLeastOneWorked
    }

    override fun close() {
        spanDiskExporter?.shutdown()
        logRecordDiskExporter?.shutdown()
        metricDiskExporter?.shutdown()
    }

    class Builder {
        private var spanDiskExporter: SpanFromDiskExporter? = null
        private var metricDiskExporter: MetricFromDiskExporter? = null
        private var logRecordDiskExporter: LogRecordFromDiskExporter? = null
        private var exportTimeoutInMillis = TimeUnit.SECONDS.toMillis(5)

        fun setSpanFromDiskExporter(spanDiskExporter: SpanFromDiskExporter?): Builder {
            this.spanDiskExporter = spanDiskExporter
            return this
        }

        fun setMetricFromDiskExporter(metricDiskExporter: MetricFromDiskExporter?): Builder {
            this.metricDiskExporter = metricDiskExporter
            return this
        }

        fun setLogRecordFromDiskExporter(logRecordDiskExporter: LogRecordFromDiskExporter?): Builder {
            this.logRecordDiskExporter = logRecordDiskExporter
            return this
        }

        fun setExportTimeoutInMillis(exportTimeoutInMillis: Long): Builder {
            this.exportTimeoutInMillis = exportTimeoutInMillis
            return this
        }

        fun build(): SignalFromDiskExporter {
            return SignalFromDiskExporter(
                spanDiskExporter,
                metricDiskExporter,
                logRecordDiskExporter,
                exportTimeoutInMillis
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
