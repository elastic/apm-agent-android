package co.elastic.apm.android.sdk.traces.connectivity;

import co.elastic.apm.android.sdk.traces.otel.exporter.ElasticSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class CommonConnectivity implements Connectivity {
    private final String endpoint;
    private String token;

    CommonConnectivity(String endpoint) {
        this.endpoint = endpoint;
    }

    private SpanExporter getSpanExporter() {
        OtlpGrpcSpanExporterBuilder exporterBuilder = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint);
        if (token != null) {
            exporterBuilder.addHeader("Authorization", "Bearer " + token);
        }
        return new ElasticSpanExporter(exporterBuilder.build());
    }

    public CommonConnectivity withAuthToken(String token) {
        this.token = token;
        return this;
    }

    @Override
    public SpanProcessor getSpanProcessor() {
        return BatchSpanProcessor.builder(getSpanExporter()).build();
    }
}