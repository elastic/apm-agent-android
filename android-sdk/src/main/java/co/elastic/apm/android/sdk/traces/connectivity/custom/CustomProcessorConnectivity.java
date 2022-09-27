package co.elastic.apm.android.sdk.traces.connectivity.custom;

import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class CustomProcessorConnectivity implements Connectivity {
    private final SpanProcessor processor;

    public CustomProcessorConnectivity(SpanProcessor processor) {
        this.processor = processor;
    }

    @Override
    public SpanProcessor getSpanProcessor() {
        return processor;
    }
}
