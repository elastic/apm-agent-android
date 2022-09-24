package co.elastic.apm.android.sdk.traces.connectivity;

import io.opentelemetry.sdk.trace.SpanProcessor;

public class CustomConnectivity implements Connectivity {
    private final SpanProcessor processor;

    CustomConnectivity(SpanProcessor processor) {
        this.processor = processor;
    }

    @Override
    public SpanProcessor getSpanProcessor() {
        return processor;
    }
}
