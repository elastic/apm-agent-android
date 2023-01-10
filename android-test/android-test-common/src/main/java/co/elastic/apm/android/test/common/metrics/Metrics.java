package co.elastic.apm.android.test.common.metrics;

import co.elastic.apm.android.test.common.metrics.verifiers.MetricVerifier;
import io.opentelemetry.sdk.metrics.data.MetricData;

public class Metrics {

    public static MetricVerifier verify(MetricData metric) {
        return new MetricVerifier(metric);
    }
}
