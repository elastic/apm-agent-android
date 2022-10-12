package co.elastic.apm.android.test.common.spans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class SpanExporterCaptor implements SpanExporter {
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

    public void clearCapturedSpans() {
        capturedSpans.clear();
    }
}
