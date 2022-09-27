package co.elastic.apm.android.sdk.traces.connectivity.custom;

import co.elastic.apm.android.sdk.traces.connectivity.base.BatchProcessingConnectivity;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class CustomExporterConnectivity extends BatchProcessingConnectivity {
    private final SpanExporter exporter;

    public CustomExporterConnectivity(SpanExporter exporter) {
        this.exporter = exporter;
    }

    @Override
    protected SpanExporter provideSpanExporter() {
        return exporter;
    }
}
