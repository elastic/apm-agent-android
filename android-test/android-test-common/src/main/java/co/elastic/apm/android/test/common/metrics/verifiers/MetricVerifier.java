package co.elastic.apm.android.test.common.metrics.verifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.metrics.data.MetricData;

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
}
