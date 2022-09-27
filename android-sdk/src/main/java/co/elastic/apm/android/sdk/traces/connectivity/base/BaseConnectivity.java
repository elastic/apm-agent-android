package co.elastic.apm.android.sdk.traces.connectivity.base;

import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import co.elastic.apm.android.sdk.traces.otel.exporter.ElasticSpanExporter;
import co.elastic.apm.android.sdk.traces.otel.processor.ElasticSpanProcessor;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public abstract class BaseConnectivity implements Connectivity {
    @Override
    public ElasticSpanProcessor getSpanProcessor() {
        return new ElasticSpanProcessor(provideSpanProcessor(new ElasticSpanExporter(provideSpanExporter())));
    }

    protected abstract SpanProcessor provideSpanProcessor(SpanExporter exporter);

    protected abstract SpanExporter provideSpanExporter();
}
