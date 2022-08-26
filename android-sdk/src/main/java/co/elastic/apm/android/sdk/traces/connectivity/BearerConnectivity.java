package co.elastic.apm.android.sdk.traces.connectivity;

import co.elastic.apm.android.sdk.traces.otel.exporter.provider.ExporterProvider;
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
    public ExporterProvider getExporterProvider() {
        return new Provider(endpoint, token);
    }

    static class Provider implements ExporterProvider {
        private final String endpoint;
        private final String token;

        Provider(String endpoint, String token) {
            this.endpoint = endpoint;
            this.token = token;
        }

        @Override
        public SpanExporter getExporter() {
            return OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).addHeader("Authorization", "Bearer " + token).build();
        }
    }
}
