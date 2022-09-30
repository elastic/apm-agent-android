package co.elastic.apm.android.test.utilities;

public interface SpanExporterProvider {
    DummySpanExporter getSpanExporter();
}
