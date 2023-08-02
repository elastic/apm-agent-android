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

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.contrib.disk.buffering.LogRecordDiskExporter;
import io.opentelemetry.contrib.disk.buffering.MetricDiskExporter;
import io.opentelemetry.contrib.disk.buffering.SpanDiskExporter;
import io.opentelemetry.contrib.disk.buffering.StoredBatchExporter;

public class SignalDiskExporterTest {

    private SpanDiskExporter spanDiskExporter;
    private MetricDiskExporter metricDiskExporter;
    private LogRecordDiskExporter logRecordDiskExporter;
    private static final long DEFAULT_EXPORT_TIMEOUT_IN_MILLIS = 1000;

    @Before
    public void setUp() {
        spanDiskExporter = mock(SpanDiskExporter.class);
        metricDiskExporter = mock(MetricDiskExporter.class);
        logRecordDiskExporter = mock(LogRecordDiskExporter.class);
    }

    @Test
    public void verifyExportingSpans() throws IOException {
        SignalDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);
        doReturn(true).when(spanDiskExporter).exportStoredBatch(anyLong(), any());

        assertTrue(instance.exportBatchOfSpans());

        verifyExportStoredBatchCall(spanDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyNoInteractions(metricDiskExporter, logRecordDiskExporter);
    }

    @Test
    public void verifyExportingMetrics() throws IOException {
        SignalDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);
        doReturn(true).when(metricDiskExporter).exportStoredBatch(anyLong(), any());

        assertTrue(instance.exportBatchOfMetrics());

        verifyExportStoredBatchCall(metricDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyNoInteractions(spanDiskExporter, logRecordDiskExporter);
    }

    @Test
    public void verifyExportingLogs() throws IOException {
        SignalDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);
        doReturn(true).when(logRecordDiskExporter).exportStoredBatch(anyLong(), any());

        assertTrue(instance.exportBatchOfLogs());

        verifyExportStoredBatchCall(logRecordDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyNoInteractions(metricDiskExporter, spanDiskExporter);
    }

    private void verifyExportStoredBatchCall(StoredBatchExporter exporter, long timeoutInMillis) throws IOException {
        verify(exporter).exportStoredBatch(timeoutInMillis, TimeUnit.MILLISECONDS);
    }


    private SignalDiskExporter createInstance(SpanDiskExporter spanDiskExporter, MetricDiskExporter metricDiskExporter, LogRecordDiskExporter logRecordDiskExporter
    ) {
        return createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
    }

    private SignalDiskExporter createInstance(SpanDiskExporter spanDiskExporter, MetricDiskExporter metricDiskExporter, LogRecordDiskExporter
            logRecordDiskExporter, long exportTimeoutInMillis) {
        return new SignalDiskExporter.Builder()
                .setSpanDiskExporter(spanDiskExporter)
                .setMetricDiskExporter(metricDiskExporter)
                .setLogRecordDiskExporter(logRecordDiskExporter)
                .setExportTimeoutInMillis(exportTimeoutInMillis)
                .build();
    }
}