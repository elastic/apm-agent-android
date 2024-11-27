package co.elastic.apm.android.test.attributes.metrics;

import static org.mockito.Mockito.doReturn;

import org.junit.Test;

import co.elastic.apm.android.test.common.metrics.Metrics;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.metrics.data.MetricData;

public class ClockTest extends BaseRobolectricTest {

    @Test
    public void whenAMetricIsCreated_itHasTimestampSetFromElasticClock() {
        long startTimeFromElasticClock = 123456789;
        setNow(startTimeFromElasticClock);
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .startedAt(startTimeFromElasticClock);
    }

    private void setNow(long now) {
        doReturn(now).when(getAgentDependenciesInjector().getElasticClock()).now();
    }

    private MetricData captureMetric() {
        MetricAttrHost host = new MetricAttrHost();

        host.methodWithCounter();

        flushMetrics();

        return getRecordedMetric();
    }
}