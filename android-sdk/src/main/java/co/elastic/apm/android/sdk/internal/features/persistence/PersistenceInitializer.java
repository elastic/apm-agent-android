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

import java.io.IOException;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.ExporterVisitor;
import co.elastic.apm.android.sdk.features.persistence.SignalFromDiskExporter;
import co.elastic.apm.android.sdk.features.persistence.SimpleTemporaryFileProvider;
import io.opentelemetry.contrib.disk.buffering.LogRecordFromDiskExporter;
import io.opentelemetry.contrib.disk.buffering.LogRecordToDiskExporter;
import io.opentelemetry.contrib.disk.buffering.MetricFromDiskExporter;
import io.opentelemetry.contrib.disk.buffering.MetricToDiskExporter;
import io.opentelemetry.contrib.disk.buffering.SpanFromDiskExporter;
import io.opentelemetry.contrib.disk.buffering.SpanToDiskExporter;
import io.opentelemetry.contrib.disk.buffering.StorageConfiguration;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public final class PersistenceInitializer implements ExporterVisitor {
    private SignalFromDiskExporter.Builder signalFromDiskExporterBuilder;
    private StorageConfiguration storageConfiguration;

    public void prepare() throws IOException {
        DiskManager diskManager = DiskManager.create();
        storageConfiguration = StorageConfiguration.builder()
                .setMaxFileSize(diskManager.getMaxCacheFileSize())
                .setMaxFolderSize(diskManager.getMaxFolderSize())
                .setTemporaryFileProvider(new SimpleTemporaryFileProvider(diskManager.getTemporaryDir()))
                .setRootDir(diskManager.getSignalsCacheDir())
                .build();
        signalFromDiskExporterBuilder = SignalFromDiskExporter.builder();
    }

    public SignalFromDiskExporter createSignalDiskExporter() {
        if (signalFromDiskExporterBuilder != null) {
            return signalFromDiskExporterBuilder.build();
        }
        throw new IllegalStateException("You must call prepare() first");
    }

    @Override
    public <T> T visitExporter(T exporter) {
        Elog.getLogger().debug("Visiting exporter: {}", exporter);
        if (signalFromDiskExporterBuilder != null) {
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
            signalFromDiskExporterBuilder.setSpanFromDiskExporter(SpanFromDiskExporter.create((SpanExporter) exporter, storageConfiguration));
            return (T) SpanToDiskExporter.create((SpanExporter) exporter, storageConfiguration);
        } else if (exporter instanceof MetricExporter) {
            signalFromDiskExporterBuilder.setMetricFromDiskExporter(MetricFromDiskExporter.create((MetricExporter) exporter, storageConfiguration));
            return (T) MetricToDiskExporter.create((MetricExporter) exporter, storageConfiguration, ((MetricExporter) exporter)::getAggregationTemporality);
        } else if (exporter instanceof LogRecordExporter) {
            signalFromDiskExporterBuilder.setLogRecordFromDiskExporter(LogRecordFromDiskExporter.create((LogRecordExporter) exporter, storageConfiguration));
            return (T) LogRecordToDiskExporter.create((LogRecordExporter) exporter, storageConfiguration);
        }
        throw new IllegalArgumentException("Could not wrap exporter of type: " + exporter.getClass());
    }
}
