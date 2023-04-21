package co.elastic.apm.android.test.common.metrics.verifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;

public class MetricVerifier {
    private final MetricData metric;

    public MetricVerifier(MetricData metric) {
        this.metric = metric;
    }

    public MetricVerifier hasResource(String resourceName) {
        assertNotNull(metric.getResource().getAttribute(AttributeKey.stringKey(resourceName)));
        return this;
    }

    public MetricVerifier hasResource(String resourceName, String resourceValue) {
        assertEquals(resourceValue, metric.getResource().getAttribute(AttributeKey.stringKey(resourceName)));
        return this;
    }

    public MetricVerifier hasResource(String resourceName, Integer resourceValue) {
        assertEquals(Long.valueOf(resourceValue), metric.getResource().getAttribute(AttributeKey.longKey(resourceName)));
        return this;
    }

    public MetricVerifier startedAt(long timeInNanoseconds) {
        List<PointData> points = new ArrayList<>(metric.getData().getPoints());
        assertEquals(timeInNanoseconds, points.get(0).getEpochNanos());
        return this;
    }

    public MetricVerifier isNamed(String name) {
        assertEquals(name, metric.getName());
        return this;
    }
}
