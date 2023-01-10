package co.elastic.apm.android.test.common.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

public class MetricExporterCaptor implements MetricExporter {

    private final List<List<MetricData>> capturedMetrics = new ArrayList<>();

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
        capturedMetrics.add(new ArrayList<>(metrics));
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

    public List<List<MetricData>> getCapturedMetrics() {
        return Collections.unmodifiableList(capturedMetrics);
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return null;
    }

    public void clearCapturedMetrics() {
        capturedMetrics.clear();
    }
}
