package co.elastic.apm.android.sdk.traces.connectivity;

import io.opentelemetry.sdk.trace.export.SpanExporter;

public class CustomConnectivity implements Connectivity {
    private final SpanExporter spanExporter;

    CustomConnectivity(SpanExporter spanExporter) {
        this.spanExporter = spanExporter;
    }

    @Override
    public SpanExporter getSpanExporter() {
        return spanExporter;
    }
}
