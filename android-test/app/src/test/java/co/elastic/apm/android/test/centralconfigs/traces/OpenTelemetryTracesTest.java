package co.elastic.apm.android.test.centralconfigs.traces;

import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.robolectric.annotation.Config;

import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.AllInstrumentationConfiguration;
import co.elastic.apm.android.sdk.traces.ElasticTracers;
import co.elastic.apm.android.test.centralconfigs.common.AppUseCases;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

public class OpenTelemetryTracesTest extends BaseRobolectricTest {

    @Test
    public void whenRecordingIsEnabled_exportTraces() {
        Tracer tracer = ElasticTracers.create("example");
        Span span = tracer.spanBuilder("someSpan").startSpan();
        span.end();

        getRecordedSpans(1);
    }

    @Test
    public void whenRecordingIsNotEnabled_doNotExportTraces() {
        doReturn(false).when(Configurations.get(AllInstrumentationConfiguration.class)).isEnabled();

        Tracer tracer = ElasticTracers.create("example");
        Span span = tracer.spanBuilder("someSpan").startSpan();
        span.end();

        getRecordedSpans(0);
    }

    @Config(application = AppUseCases.AppWithSampleRateZero.class)
    @Test
    public void whenSampleRateIsZero_doNotExportTraces() {
        Tracer tracer = ElasticTracers.create("example");
        Span span = tracer.spanBuilder("someSpan").startSpan();
        span.end();

        getRecordedSpans(0);
    }
}
