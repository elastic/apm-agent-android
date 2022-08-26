package co.elastic.apm.android.sdk.traces.connectivity;

import io.opentelemetry.sdk.trace.export.SpanExporter;

public interface Connectivity {

    static CommonConnectivity create(String endpoint) {
        return new CommonConnectivity(endpoint);
    }

    static Connectivity custom(SpanExporter exporter) {
        return new CustomConnectivity(exporter);
    }

    SpanExporter getSpanExporter();
}
