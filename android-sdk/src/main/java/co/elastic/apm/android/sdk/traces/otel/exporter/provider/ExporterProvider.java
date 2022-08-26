package co.elastic.apm.android.sdk.traces.otel.exporter.provider;

import io.opentelemetry.sdk.trace.export.SpanExporter;

public interface ExporterProvider {
    SpanExporter getExporter();
}
