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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.internal.features.persistence.DiskManager;
import io.opentelemetry.contrib.disk.buffering.LogRecordDiskExporter;
import io.opentelemetry.contrib.disk.buffering.MetricDiskExporter;
import io.opentelemetry.contrib.disk.buffering.SpanDiskExporter;
import io.opentelemetry.contrib.disk.buffering.internal.StorageConfiguration;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public final class SignalDiskExporter {
    private final SpanDiskExporter spanDiskExporter;
    private final MetricDiskExporter metricDiskExporter;
    private final LogRecordDiskExporter logRecordDiskExporter;
    private final long exportTimeoutInMillis;

    SignalDiskExporter(SpanDiskExporter spanDiskExporter, MetricDiskExporter metricDiskExporter, LogRecordDiskExporter logRecordDiskExporter, long exportTimeoutInMillis) {
        this.spanDiskExporter = spanDiskExporter;
        this.metricDiskExporter = metricDiskExporter;
        this.logRecordDiskExporter = logRecordDiskExporter;
        this.exportTimeoutInMillis = exportTimeoutInMillis;
    }

    public static Builder builder() throws IOException {
        DiskManager diskManager = DiskManager.create();
        StorageConfiguration storageConfiguration = StorageConfiguration.builder()
                .setMaxFileSize(diskManager.getMaxCacheFileSize())
                .setMaxFolderSize(diskManager.getMaxFolderSize())
                .setTemporaryFileProvider(new SimpleTemporaryFileProvider(diskManager.getTemporaryDir()))
                .build();
        return new Builder(diskManager.getSignalsCacheDir(), storageConfiguration);
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
        private final File rootDir;
        private final StorageConfiguration storageConfiguration;
        private SpanExporter spanExporter;
        private MetricExporter metricExporter;
        private LogRecordExporter logRecordExporter;
        private long exportTimeoutInMillis = TimeUnit.SECONDS.toMillis(5);

        private Builder(File rootDir, StorageConfiguration storageConfiguration) {
            this.rootDir = rootDir;
            this.storageConfiguration = storageConfiguration;
        }

        public Builder setSpanExporter(SpanExporter spanExporter) {
            this.spanExporter = spanExporter;
            return this;
        }

        public Builder setMetricExporter(MetricExporter metricExporter) {
            this.metricExporter = metricExporter;
            return this;
        }

        public Builder setLogRecordExporter(LogRecordExporter logRecordExporter) {
            this.logRecordExporter = logRecordExporter;
            return this;
        }

        public Builder setExportTimeoutInMillis(long exportTimeoutInMillis) {
            this.exportTimeoutInMillis = exportTimeoutInMillis;
            return this;
        }

        public SignalDiskExporter build() throws IOException {
            SpanDiskExporter spanDiskExporter = (spanExporter != null) ? SpanDiskExporter.create(spanExporter, rootDir, storageConfiguration) : null;
            MetricDiskExporter metricDiskExporter = (metricExporter != null) ? MetricDiskExporter.create(metricExporter, rootDir, storageConfiguration) : null;
            LogRecordDiskExporter logRecordDiskExporter = (logRecordExporter != null) ? LogRecordDiskExporter.create(logRecordExporter, rootDir, storageConfiguration) : null;
            return new SignalDiskExporter(spanDiskExporter, metricDiskExporter, logRecordDiskExporter, exportTimeoutInMillis);
        }
    }
}
