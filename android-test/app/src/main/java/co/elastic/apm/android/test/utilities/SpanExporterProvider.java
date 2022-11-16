package co.elastic.apm.android.test.utilities;

import co.elastic.apm.android.test.common.spans.SpanExporterCaptor;

public interface SpanExporterProvider {
    SpanExporterCaptor getSpanExporter();
}
