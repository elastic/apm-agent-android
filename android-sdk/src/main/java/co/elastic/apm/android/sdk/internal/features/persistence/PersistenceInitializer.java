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
package co.elastic.apm.android.sdk.internal.features.persistence;

import java.io.File;
import java.io.IOException;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.ExporterVisitor;
import co.elastic.apm.android.sdk.features.persistence.SignalDiskExporter;
import co.elastic.apm.android.sdk.features.persistence.SimpleTemporaryFileProvider;
import io.opentelemetry.contrib.disk.buffering.LogRecordDiskExporter;
import io.opentelemetry.contrib.disk.buffering.MetricDiskExporter;
import io.opentelemetry.contrib.disk.buffering.SpanDiskExporter;
import io.opentelemetry.contrib.disk.buffering.internal.StorageConfiguration;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public final class PersistenceInitializer implements ExporterVisitor {
    private SignalDiskExporter.Builder signalDiskExporterBuilder;
    private StorageConfiguration storageConfiguration;
    private File signalsDir;

    public void prepare() throws IOException {
        DiskManager diskManager = DiskManager.create();
        storageConfiguration = StorageConfiguration.builder()
                .setMaxFileSize(diskManager.getMaxCacheFileSize())
                .setMaxFolderSize(diskManager.getMaxFolderSize())
                .setTemporaryFileProvider(new SimpleTemporaryFileProvider(diskManager.getTemporaryDir()))
                .build();
        signalsDir = diskManager.getSignalsCacheDir();
        signalDiskExporterBuilder = SignalDiskExporter.builder();
    }

    public SignalDiskExporter createSignalDiskExporter() {
        if (signalDiskExporterBuilder != null) {
            return signalDiskExporterBuilder.build();
        }
        throw new IllegalStateException("You must call prepare() first");
    }

    @Override
    public <T> T visitExporter(T exporter) {
        Elog.getLogger().debug("Visiting exporter: {}", exporter);
        if (signalDiskExporterBuilder != null) {
            try {
                return persistExporterSignals(exporter);
            } catch (Exception e) {
                Elog.getLogger().error("Could not persist exporter: " + exporter, e);
                return exporter;
            }
        }
        return exporter;
    }

    @SuppressWarnings("unchecked")
    private <T> T persistExporterSignals(T exporter) throws IOException {
        if (exporter instanceof SpanExporter) {
            SpanDiskExporter spanDiskExporter = SpanDiskExporter.create((SpanExporter) exporter, signalsDir, storageConfiguration);
            signalDiskExporterBuilder.setSpanDiskExporter(spanDiskExporter);
            return (T) spanDiskExporter;
        } else if (exporter instanceof MetricExporter) {
            MetricDiskExporter metricDiskExporter = MetricDiskExporter.create((MetricExporter) exporter, signalsDir, storageConfiguration);
            signalDiskExporterBuilder.setMetricDiskExporter(metricDiskExporter);
            return (T) metricDiskExporter;
        } else if (exporter instanceof LogRecordExporter) {
            LogRecordDiskExporter logRecordDiskExporter = LogRecordDiskExporter.create((LogRecordExporter) exporter, signalsDir, storageConfiguration);
            signalDiskExporterBuilder.setLogRecordDiskExporter(logRecordDiskExporter);
            return (T) logRecordDiskExporter;
        }
        throw new IllegalArgumentException("Could not wrap exporter of type: " + exporter.getClass());
    }
}
