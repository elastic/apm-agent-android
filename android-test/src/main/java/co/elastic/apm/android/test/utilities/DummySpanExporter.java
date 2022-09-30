package co.elastic.apm.android.test.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class DummySpanExporter implements SpanExporter {
    private final List<List<SpanData>> capturedSpans = new ArrayList<>();

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        capturedSpans.add(new ArrayList<>(spans));
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    public List<List<SpanData>> getCapturedSpans() {
        return Collections.unmodifiableList(capturedSpans);
    }
}
