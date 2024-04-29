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

import static org.junit.Assert.assertFalse;
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

import io.opentelemetry.contrib.disk.buffering.LogRecordFromDiskExporter;
import io.opentelemetry.contrib.disk.buffering.MetricFromDiskExporter;
import io.opentelemetry.contrib.disk.buffering.SpanFromDiskExporter;
import io.opentelemetry.contrib.disk.buffering.internal.exporter.FromDiskExporter;

public class SignalFromDiskExporterTest {

    private SpanFromDiskExporter spanDiskExporter;
    private MetricFromDiskExporter metricDiskExporter;
    private LogRecordFromDiskExporter logRecordDiskExporter;
    private static final long DEFAULT_EXPORT_TIMEOUT_IN_MILLIS = 1000;

    @Before
    public void setUp() {
        spanDiskExporter = mock(SpanFromDiskExporter.class);
        metricDiskExporter = mock(MetricFromDiskExporter.class);
        logRecordDiskExporter = mock(LogRecordFromDiskExporter.class);
    }

    @Test
    public void verifyExportingSpans() throws IOException {
        SignalFromDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);
        doReturn(true).when(spanDiskExporter).exportStoredBatch(anyLong(), any());

        assertTrue(instance.exportBatchOfSpans());

        verifyExportStoredBatchCall(spanDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyNoInteractions(metricDiskExporter, logRecordDiskExporter);
    }

    @Test
    public void verifyExportingMetrics() throws IOException {
        SignalFromDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);
        doReturn(true).when(metricDiskExporter).exportStoredBatch(anyLong(), any());

        assertTrue(instance.exportBatchOfMetrics());

        verifyExportStoredBatchCall(metricDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyNoInteractions(spanDiskExporter, logRecordDiskExporter);
    }

    @Test
    public void verifyExportingLogs() throws IOException {
        SignalFromDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);
        doReturn(true).when(logRecordDiskExporter).exportStoredBatch(anyLong(), any());

        assertTrue(instance.exportBatchOfLogs());

        verifyExportStoredBatchCall(logRecordDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyNoInteractions(metricDiskExporter, spanDiskExporter);
    }

    @Test
    public void verifyExportingEach_whenAllReturnFalse() throws IOException {
        SignalFromDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);

        assertFalse(instance.exportBatchOfEach());

        verifyExportStoredBatchCall(spanDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyExportStoredBatchCall(metricDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyExportStoredBatchCall(logRecordDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
    }

    @Test
    public void verifyExportingEach_whenSpansReturnTrue() throws IOException {
        SignalFromDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);
        doReturn(true).when(spanDiskExporter).exportStoredBatch(anyLong(), any());

        assertTrue(instance.exportBatchOfEach());

        verifyExportStoredBatchCall(spanDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyExportStoredBatchCall(metricDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyExportStoredBatchCall(logRecordDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
    }

    @Test
    public void verifyExportingEach_whenMetricsReturnTrue() throws IOException {
        SignalFromDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);
        doReturn(true).when(metricDiskExporter).exportStoredBatch(anyLong(), any());

        assertTrue(instance.exportBatchOfEach());

        verifyExportStoredBatchCall(spanDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyExportStoredBatchCall(metricDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyExportStoredBatchCall(logRecordDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
    }

    @Test
    public void verifyExportingEach_whenLogsReturnTrue() throws IOException {
        SignalFromDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter);
        doReturn(true).when(logRecordDiskExporter).exportStoredBatch(anyLong(), any());

        assertTrue(instance.exportBatchOfEach());

        verifyExportStoredBatchCall(spanDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyExportStoredBatchCall(metricDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
        verifyExportStoredBatchCall(logRecordDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
    }

    @Test
    public void whenSpansExporterIsNull_returnFalse() throws IOException {
        SignalFromDiskExporter instance = createInstance(null, metricDiskExporter, logRecordDiskExporter);

        assertFalse(instance.exportBatchOfSpans());
    }


    @Test
    public void whenMetricsExporterIsNull_returnFalse() throws IOException {
        SignalFromDiskExporter instance = createInstance(spanDiskExporter, null, logRecordDiskExporter);

        assertFalse(instance.exportBatchOfMetrics());
    }

    @Test
    public void whenLogsExporterIsNull_returnFalse() throws IOException {
        SignalFromDiskExporter instance = createInstance(spanDiskExporter, metricDiskExporter, null);

        assertFalse(instance.exportBatchOfLogs());
    }

    private void verifyExportStoredBatchCall(FromDiskExporter exporter, long timeoutInMillis) throws IOException {
        verify(exporter).exportStoredBatch(timeoutInMillis, TimeUnit.MILLISECONDS);
    }


    private SignalFromDiskExporter createInstance(SpanFromDiskExporter spanDiskExporter, MetricFromDiskExporter metricDiskExporter, LogRecordFromDiskExporter logRecordDiskExporter
    ) {
        return createInstance(spanDiskExporter, metricDiskExporter, logRecordDiskExporter, DEFAULT_EXPORT_TIMEOUT_IN_MILLIS);
    }

    private SignalFromDiskExporter createInstance(SpanFromDiskExporter spanDiskExporter, MetricFromDiskExporter metricDiskExporter, LogRecordFromDiskExporter
            logRecordDiskExporter, long exportTimeoutInMillis) {
        return new SignalFromDiskExporter(spanDiskExporter, metricDiskExporter, logRecordDiskExporter, exportTimeoutInMillis);
    }
}