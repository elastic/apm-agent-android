package co.elastic.apm.android.sdk.traces.connectivity;

import co.elastic.apm.android.sdk.traces.connectivity.custom.CustomExporterConnectivity;
import co.elastic.apm.android.sdk.traces.connectivity.custom.CustomProcessorConnectivity;
import co.elastic.apm.android.sdk.traces.otel.processor.ElasticSpanProcessor;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public interface Connectivity {

    static CommonConnectivity create(String endpoint) {
        return new CommonConnectivity(endpoint);
    }

    static Connectivity custom(SpanExporter exporter) {
        return new CustomExporterConnectivity(exporter);
    }

    static Connectivity custom(SpanProcessor processor) {
        return new CustomProcessorConnectivity(processor);
    }

    ElasticSpanProcessor getSpanProcessor();
}
