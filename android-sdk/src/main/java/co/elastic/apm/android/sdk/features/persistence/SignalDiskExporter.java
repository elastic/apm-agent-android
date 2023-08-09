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
package co.elastic.apm.android.sdk.features.persistence;

import androidx.annotation.WorkerThread;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.common.internal.logging.Elog;
import io.opentelemetry.contrib.disk.buffering.LogRecordDiskExporter;
import io.opentelemetry.contrib.disk.buffering.MetricDiskExporter;
import io.opentelemetry.contrib.disk.buffering.SpanDiskExporter;

public final class SignalDiskExporter {
    private static SignalDiskExporter instance;
    private final SpanDiskExporter spanDiskExporter;
    private final MetricDiskExporter metricDiskExporter;
    private final LogRecordDiskExporter logRecordDiskExporter;
    private final long exportTimeoutInMillis;

    public static SignalDiskExporter get() {
        Elog.getLogger().debug("Getting SignalDiskExporter");
        if (instance == null) {
            Elog.getLogger().debug("Returning noop SignalDiskExporter");
            return new SignalDiskExporter(null, null, null, 0);
        }
        return instance;
    }

    public static void set(SignalDiskExporter signalDiskExporter) {
        Elog.getLogger().debug("Setting SignalDiskExporter");
        if (instance != null) {
            throw new IllegalStateException("An instance is already set. You can only set it once.");
        }
        instance = signalDiskExporter;
    }

    SignalDiskExporter(SpanDiskExporter spanDiskExporter, MetricDiskExporter metricDiskExporter, LogRecordDiskExporter logRecordDiskExporter, long exportTimeoutInMillis) {
        this.spanDiskExporter = spanDiskExporter;
        this.metricDiskExporter = metricDiskExporter;
        this.logRecordDiskExporter = logRecordDiskExporter;
        this.exportTimeoutInMillis = exportTimeoutInMillis;
    }

    public static Builder builder() throws IOException {
        return new Builder();
    }

    @WorkerThread
    public boolean exportBatchOfSpans() throws IOException {
        if (spanDiskExporter == null) {
            return false;
        }
        return spanDiskExporter.exportStoredBatch(exportTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @WorkerThread
    public boolean exportBatchOfMetrics() throws IOException {
        if (metricDiskExporter == null) {
            return false;
        }
        return metricDiskExporter.exportStoredBatch(exportTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @WorkerThread
    public boolean exportBatchOfLogs() throws IOException {
        if (logRecordDiskExporter == null) {
            return false;
        }
        return logRecordDiskExporter.exportStoredBatch(exportTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @WorkerThread
    public boolean exportBatchOfEach() throws IOException {
        boolean atLeastOneWorked = exportBatchOfSpans();
        if (exportBatchOfMetrics()) {
            atLeastOneWorked = true;
        }
        if (exportBatchOfLogs()) {
            atLeastOneWorked = true;
        }
        return atLeastOneWorked;
    }

    public static class Builder {
        private SpanDiskExporter spanDiskExporter;
        private MetricDiskExporter metricDiskExporter;
        private LogRecordDiskExporter logRecordDiskExporter;
        private long exportTimeoutInMillis = TimeUnit.SECONDS.toMillis(5);

        private Builder() {
        }

        public Builder setSpanDiskExporter(SpanDiskExporter spanDiskExporter) {
            this.spanDiskExporter = spanDiskExporter;
            return this;
        }

        public Builder setMetricDiskExporter(MetricDiskExporter metricDiskExporter) {
            this.metricDiskExporter = metricDiskExporter;
            return this;
        }

        public Builder setLogRecordDiskExporter(LogRecordDiskExporter logRecordDiskExporter) {
            this.logRecordDiskExporter = logRecordDiskExporter;
            return this;
        }

        public Builder setExportTimeoutInMillis(long exportTimeoutInMillis) {
            this.exportTimeoutInMillis = exportTimeoutInMillis;
            return this;
        }

        public SignalDiskExporter build() {
            return new SignalDiskExporter(spanDiskExporter, metricDiskExporter, logRecordDiskExporter, exportTimeoutInMillis);
        }
    }
}
