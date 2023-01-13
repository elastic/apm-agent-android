package co.elastic.apm.android.test.common.metrics;

import io.opentelemetry.sdk.metrics.export.MetricReader;

public class MetricsFlusher {
    private final MetricReader reader;

    public MetricsFlusher(MetricReader reader) {
        this.reader = reader;
    }

    public void flush() {
        reader.forceFlush();
    }
}
