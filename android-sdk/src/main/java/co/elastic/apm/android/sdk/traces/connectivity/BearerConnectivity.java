package co.elastic.apm.android.sdk.traces.connectivity;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class BearerConnectivity implements Connectivity {
    private final String endpoint;
    private final String token;

    public BearerConnectivity(String endpoint, String token) {
        this.endpoint = endpoint;
        this.token = token;
    }

    @Override
    public SpanExporter getSpanExporter() {
        return OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).addHeader("Authorization", "Bearer " + token).build();
    }
}
