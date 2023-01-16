package co.elastic.apm.android.test.providers;

import co.elastic.apm.android.test.common.logs.LogRecordExporterCaptor;
import co.elastic.apm.android.test.common.metrics.MetricExporterCaptor;
import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;

public interface ExportersProvider {
    SpanExporterCaptor getSpanExporter();

    MetricExporterCaptor getMetricExporter();

    LogRecordExporterCaptor getLogRecordExporter();
}
