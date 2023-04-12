package co.elastic.apm.android.test.attributes.metrics;

import static co.elastic.apm.android.test.attributes.common.ResourcesApp.DEVICE_MANUFACTURER;
import static co.elastic.apm.android.test.attributes.common.ResourcesApp.DEVICE_MODEL_NAME;
import static co.elastic.apm.android.test.attributes.common.ResourcesApp.RUNTIME_VERSION;

import org.junit.Test;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.test.BuildConfig;
import co.elastic.apm.android.test.attributes.common.ResourcesApp;
import co.elastic.apm.android.test.common.metrics.Metrics;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.metrics.data.MetricData;

@Config(application = ResourcesApp.class)
public class ResourcesTest extends BaseRobolectricTest {

    @Test
    public void whenAMetricIsCreated_serviceNameIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("service.name", "my-app");
    }

    @Test
    public void whenAMetricIsCreated_serviceVersionIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("service.version", "1.0(5)");
    }

    @Test
    public void whenAMetricIsCreated_serviceBuildIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("service.build", 5);
    }

    @Test
    public void whenAMetricIsCreated_agentNameIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("telemetry.sdk.name", "android");
    }

    @Test
    public void whenAMetricIsCreated_agentVersionIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("telemetry.sdk.version", System.getProperty("agentVersion"));
    }

    @Test
    public void whenAMetricIsCreated_osDescriptionIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("os.description", "Android 12, API level 32, BUILD unknown");
    }

    @Test
    public void whenAMetricIsCreated_osNameIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("os.name", "Android");
    }

    @Test
    public void whenAMetricIsCreated_osVersionSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("os.version", "12");
    }

    @Test
    public void whenAMetricIsCreated_deploymentEnvironmentIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("deployment.environment", (BuildConfig.DEBUG) ? "debug" : "release");
    }

    @Test
    public void whenAMetricIsCreated_deviceIdIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("device.id");
    }

    @Test
    public void whenAMetricIsCreated_deviceModelIdIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("device.model.identifier", DEVICE_MODEL_NAME);
    }

    @Test
    public void whenAMetricIsCreated_deviceModelManufacturerIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("device.manufacturer", DEVICE_MANUFACTURER);
    }

    @Test
    public void whenAMetricIsCreated_processRuntimeNameIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("process.runtime.name", "Android Runtime");
    }

    @Test
    public void whenAMetricIsCreated_processRuntimeVersionIsSet() {
        MetricData metric = captureMetric();

        Metrics.verify(metric)
                .hasResource("process.runtime.version", RUNTIME_VERSION);
    }

    private MetricData captureMetric() {
        MetricAttrHost host = new MetricAttrHost();

        host.methodWithCounter();
        flushMetrics();

        return getRecordedMetric();
    }
}
