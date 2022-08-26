package co.elastic.apm.android.sdk.traces.connectivity;

import io.opentelemetry.sdk.trace.export.SpanExporter;

public interface Connectivity {

    static Connectivity simple(String endpoint) {
        return new SimpleConnectivity(endpoint);
    }

    static Connectivity token(String endpoint, String token) {
        return new BearerConnectivity(endpoint, token);
    }

    static Connectivity custom(SpanExporter exporter) {
        return new CustomConnectivity(exporter);
    }

    SpanExporter getSpanExporter();
}
