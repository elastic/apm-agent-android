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
import co.elastic.otel.android.internal.features.diskbuffering.tools.FromDiskExporter
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.trace.data.SpanData
import java.io.Closeable

/**
 * Entrypoint to read and export previously cached signals.
 *
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class SignalFromDiskExporter internal constructor(
    private val spanFromDiskExporter: FromDiskExporter<SpanData>?,
    private val metricFromDiskExporter: FromDiskExporter<MetricData>?,
    private val logRecordFromDiskExporter: FromDiskExporter<LogRecordData>?,
) : Closeable {
    private val logger = Elog.getLogger()

    @WorkerThread
    fun exportBatchOfSpans(): Boolean {
        return try {
            spanFromDiskExporter?.exportNextBatch() ?: false
        } catch (t: Throwable) {
            logger.error("Error while trying to export spans from disk", t)
            false
        }
    }

    @WorkerThread
    fun exportBatchOfMetrics(): Boolean {
        return try {
            metricFromDiskExporter?.exportNextBatch() ?: false
        } catch (t: Throwable) {
            logger.error("Error while trying to export metrics from disk", t)
            false
        }
    }

    @WorkerThread
    fun exportBatchOfLogs(): Boolean {
        return try {
            logRecordFromDiskExporter?.exportNextBatch() ?: false
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
        spanFromDiskExporter?.close()
        logRecordFromDiskExporter?.close()
        metricFromDiskExporter?.close()
    }

    class Builder {
        private var spanFromDiskExporter: FromDiskExporter<SpanData>? = null
        private var metricFromDiskExporter: FromDiskExporter<MetricData>? = null
        private var logRecordFromDiskExporter: FromDiskExporter<LogRecordData>? = null

        fun setSpanFromDiskExporter(value: FromDiskExporter<SpanData>?): Builder {
            this.spanFromDiskExporter = value
            return this
        }

        fun setMetricFromDiskExporter(value: FromDiskExporter<MetricData>?): Builder {
            this.metricFromDiskExporter = value
            return this
        }

        fun setLogRecordFromDiskExporter(value: FromDiskExporter<LogRecordData>?): Builder {
            this.logRecordFromDiskExporter = value
            return this
        }

        fun build(): SignalFromDiskExporter {
            return SignalFromDiskExporter(
                spanFromDiskExporter,
                metricFromDiskExporter,
                logRecordFromDiskExporter
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
