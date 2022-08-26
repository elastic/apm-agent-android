package co.elastic.apm.android.sdk.traces.connectivity;

import co.elastic.apm.android.sdk.traces.otel.exporter.provider.ExporterProvider;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class SimpleConnectivity implements Connectivity {
    private final String endpoint;

    public SimpleConnectivity(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public ExporterProvider getExporterProvider() {
        return new Provider(endpoint);
    }

    static class Provider implements ExporterProvider {
        private final String endpoint;

        Provider(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public SpanExporter getExporter() {
            return OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();
        }
    }
}
