package co.elastic.apm.android.test.attributes.metrics;

import org.junit.Test;

import co.elastic.apm.android.test.common.metrics.Metrics;
import co.elastic.apm.android.test.testutils.TestElasticClock;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.metrics.data.MetricData;

public class ClockTest extends BaseRobolectricTest {

    @Test
    public void whenAMetricIsCreated_itHasTimestampSetFromElasticClock() {
        long startTimeFromElasticClock = 123456789;
        TestElasticClock clock = (TestElasticClock) getAgentDependenciesInjector().getClock();
        clock.setForcedNow(startTimeFromElasticClock);
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .startedAt(startTimeFromElasticClock);
    }

    private MetricData captureMetric() {
        MetricAttrHost host = new MetricAttrHost();

        host.methodWithCounter();

        flushMetrics();

        return getRecordedMetric();
    }
}