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
import io.opentelemetry.contrib.disk.buffering.LogRecordFromDiskExporter;
import io.opentelemetry.contrib.disk.buffering.MetricFromDiskExporter;
import io.opentelemetry.contrib.disk.buffering.SpanFromDiskExporter;

/**
 * Entrypoint to read and export previously cached signals.
 */
public final class SignalFromDiskExporter {
    private static SignalFromDiskExporter instance;
    private final SpanFromDiskExporter spanDiskExporter;
    private final MetricFromDiskExporter metricDiskExporter;
    private final LogRecordFromDiskExporter logRecordDiskExporter;
    private final long exportTimeoutInMillis;

    public static SignalFromDiskExporter get() {
        Elog.getLogger().debug("Getting SignalFromDiskExporter");
        if (instance == null) {
            Elog.getLogger().debug("Returning noop SignalFromDiskExporter");
            return new SignalFromDiskExporter(null, null, null, 0);
        }
        return instance;
    }

    public static void set(SignalFromDiskExporter signalFromDiskExporter) {
        Elog.getLogger().debug("Setting SignalFromDiskExporter");
        if (instance != null) {
            throw new IllegalStateException("An instance is already set. You can only set it once.");
        }
        instance = signalFromDiskExporter;
    }

    public static void resetForTesting() {
        instance = null;
    }

    SignalFromDiskExporter(SpanFromDiskExporter spanDiskExporter, MetricFromDiskExporter metricDiskExporter, LogRecordFromDiskExporter logRecordDiskExporter, long exportTimeoutInMillis) {
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
            Elog.getLogger().debug("Ignoring call to export batch of spans as no disk exporter is set for spans");
            return false;
        }
        return spanDiskExporter.exportStoredBatch(exportTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @WorkerThread
    public boolean exportBatchOfMetrics() throws IOException {
        if (metricDiskExporter == null) {
            Elog.getLogger().debug("Ignoring call to export batch of metrics as no disk exporter is set for metrics");
            return false;
        }
        return metricDiskExporter.exportStoredBatch(exportTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @WorkerThread
    public boolean exportBatchOfLogs() throws IOException {
        if (logRecordDiskExporter == null) {
            Elog.getLogger().debug("Ignoring call to export batch of logs as no disk exporter is set for logs");
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
        private SpanFromDiskExporter spanDiskExporter;
        private MetricFromDiskExporter metricDiskExporter;
        private LogRecordFromDiskExporter logRecordDiskExporter;
        private long exportTimeoutInMillis = TimeUnit.SECONDS.toMillis(5);

        private Builder() {
        }

        public Builder setSpanFromDiskExporter(SpanFromDiskExporter spanDiskExporter) {
            this.spanDiskExporter = spanDiskExporter;
            return this;
        }

        public Builder setMetricFromDiskExporter(MetricFromDiskExporter metricDiskExporter) {
            this.metricDiskExporter = metricDiskExporter;
            return this;
        }

        public Builder setLogRecordFromDiskExporter(LogRecordFromDiskExporter logRecordDiskExporter) {
            this.logRecordDiskExporter = logRecordDiskExporter;
            return this;
        }

        public Builder setExportTimeoutInMillis(long exportTimeoutInMillis) {
            this.exportTimeoutInMillis = exportTimeoutInMillis;
            return this;
        }

        public SignalFromDiskExporter build() {
            return new SignalFromDiskExporter(spanDiskExporter, metricDiskExporter, logRecordDiskExporter, exportTimeoutInMillis);
        }
    }
}
