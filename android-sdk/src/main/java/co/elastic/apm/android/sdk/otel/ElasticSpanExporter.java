package co.elastic.apm.android.sdk.otel;

import java.util.ArrayList;
import java.util.Collection;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class ElasticSpanExporter implements SpanExporter {
    private final SpanExporter original;

    public ElasticSpanExporter(SpanExporter original) {
        this.original = original;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        Collection<SpanData> newSpans = new ArrayList<>();

        for (SpanData item : spans) {
            newSpans.add(new TimeSkewAwareSpanData(item));
        }

        return original.export(newSpans);
    }

    @Override
    public CompletableResultCode flush() {
        return original.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return original.shutdown();
    }
}
