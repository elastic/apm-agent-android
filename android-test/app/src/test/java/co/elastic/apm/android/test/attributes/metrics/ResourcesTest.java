package co.elastic.apm.android.test.attributes.metrics;

import org.junit.Test;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.test.attributes.common.ResourcesApp;
import co.elastic.apm.android.test.common.metrics.Metrics;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.metrics.data.MetricData;

@Config(application = ResourcesApp.class)
public class ResourcesTest extends BaseRobolectricTest {

    @Test
    public void whenASpanIsCreated_serviceNameIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("service.name", "my-app");
    }

    private MetricData captureMetric() {
        MetricAttrHost host = new MetricAttrHost();

        host.methodWithCounter();

        return getRecorderMetric();
    }
}
