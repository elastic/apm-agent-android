package co.elastic.apm.android.sdk.traces.connectivity;

import co.elastic.apm.android.sdk.traces.connectivity.base.BatchProcessingConnectivity;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class CommonConnectivity extends BatchProcessingConnectivity {
    private final String endpoint;
    private String token;

    CommonConnectivity(String endpoint) {
        this.endpoint = endpoint;
    }

    public CommonConnectivity withAuthToken(String token) {
        this.token = token;
        return this;
    }

    @Override
    protected SpanExporter provideSpanExporter() {
        OtlpGrpcSpanExporterBuilder exporterBuilder = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint);
        if (token != null) {
            exporterBuilder.addHeader("Authorization", "Bearer " + token);
        }
        return exporterBuilder.build();
    }
}