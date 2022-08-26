package co.elastic.apm.android.sdk.traces.connectivity;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class SimpleConnectivity implements Connectivity {
    private final String endpoint;

    public SimpleConnectivity(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public SpanExporter getSpanExporter() {
        return OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();
    }
}
