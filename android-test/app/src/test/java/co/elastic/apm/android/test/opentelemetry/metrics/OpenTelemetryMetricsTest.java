package co.elastic.apm.android.test.opentelemetry.metrics;

import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.AllInstrumentationConfiguration;
import co.elastic.apm.android.sdk.metrics.ElasticMeters;
import co.elastic.apm.android.test.opentelemetry.common.AppUseCases;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.api.metrics.Meter;

public class OpenTelemetryMetricsTest extends BaseRobolectricTest {

    @Test
    public void whenRecordingIsEnabled_exportMetrics() {
        Meter meter = ElasticMeters.create("somemeter");
        meter.counterBuilder("somecounter").build().add(1);

        flushMetrics();

        getRecordedMetrics(1);
    }

    @Test
    public void whenRecordingIsNotEnabled_doNotExportMetrics() {
        doReturn(false).when(Configurations.get(AllInstrumentationConfiguration.class)).isEnabled();

        Meter meter = ElasticMeters.create("somemeter");
        meter.counterBuilder("somecounter").build().add(1);

        flushMetrics();

        getRecordedMetrics(0);
    }

    @Config(application = AppUseCases.AppWithSampleRateZero.class)
    @Test
    public void whenSampleRateIsZero_doNotExportMetrics() {
        Meter meter = ElasticMeters.create("somemeter");
        meter.counterBuilder("somecounter").build().add(1);

        flushMetrics();

        getRecordedMetrics(0);
    }
}
