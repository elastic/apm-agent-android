package co.elastic.apm.android.sdk.traces.connectivity;

import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public interface Connectivity {

    static CommonConnectivity create(String endpoint) {
        return new CommonConnectivity(endpoint);
    }

    static Connectivity custom(SpanExporter exporter) {
        return new CustomConnectivity(BatchSpanProcessor.builder(exporter).build());
    }

    static Connectivity custom(SpanProcessor processor) {
        return new CustomConnectivity(processor);
    }

    SpanProcessor getSpanProcessor();
}
