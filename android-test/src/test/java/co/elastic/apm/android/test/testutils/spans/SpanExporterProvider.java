package co.elastic.apm.android.test.testutils.spans;

import io.opentelemetry.sdk.trace.export.SpanExporter;

public interface SpanExporterProvider {
    SpanExporter getSpanExporter();
}
