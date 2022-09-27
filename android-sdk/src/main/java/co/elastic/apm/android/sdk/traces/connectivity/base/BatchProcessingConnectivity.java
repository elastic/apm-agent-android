package co.elastic.apm.android.sdk.traces.connectivity.base;

import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public abstract class BatchProcessingConnectivity extends BaseConnectivity {

    @Override
    protected SpanProcessor provideSpanProcessor(SpanExporter exporter) {
        return BatchSpanProcessor.builder(exporter).build();
    }
}
