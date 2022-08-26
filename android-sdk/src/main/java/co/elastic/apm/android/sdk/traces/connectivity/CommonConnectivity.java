package co.elastic.apm.android.sdk.traces.connectivity;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class CommonConnectivity implements Connectivity {
    private final String endpoint;
    private String token;

    public CommonConnectivity(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public SpanExporter getSpanExporter() {
        OtlpGrpcSpanExporterBuilder exporterBuilder = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint);
        if (token != null) {
            exporterBuilder.addHeader("Authorization", "Bearer " + token);
        }
        return exporterBuilder.build();
    }

    public CommonConnectivity withAuthToken(String token) {
        this.token = token;
        return this;
    }
}
