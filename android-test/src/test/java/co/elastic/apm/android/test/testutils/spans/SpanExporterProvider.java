package co.elastic.apm.android.test.testutils.spans;

import co.elastic.apm.android.test.testutils.base.DummySpanExporter;

public interface SpanExporterProvider {
    DummySpanExporter getSpanExporter();
}
