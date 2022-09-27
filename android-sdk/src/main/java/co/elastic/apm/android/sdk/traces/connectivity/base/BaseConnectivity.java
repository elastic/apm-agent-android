package co.elastic.apm.android.sdk.traces.connectivity.base;

import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import co.elastic.apm.android.sdk.traces.otel.exporter.ElasticSpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public abstract class BaseConnectivity implements Connectivity {

    @Override
    public SpanProcessor getSpanProcessor() {
        SpanExporter original = provideSpanExporter();
        ElasticSpanExporter exporter;
        if (original instanceof ElasticSpanExporter) {
            exporter = (ElasticSpanExporter) original;
        } else {
            exporter = new ElasticSpanExporter(original);
        }
        return provideSpanProcessor(exporter);
    }

    protected abstract SpanProcessor provideSpanProcessor(SpanExporter exporter);

    protected abstract SpanExporter provideSpanExporter();
}
