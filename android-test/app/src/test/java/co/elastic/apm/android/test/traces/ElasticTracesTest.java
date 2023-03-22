package co.elastic.apm.android.test.traces;

import static org.mockito.Mockito.doReturn;

import org.junit.Test;

import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.AllInstrumentationConfiguration;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.api.trace.Span;

public class ElasticTracesTest extends BaseRobolectricTest {

    @Test
    public void whenRecordingIsEnabled_exportTraces() {
        ElasticTracer tracer = ElasticTracer.create("example");
        Span span = tracer.spanBuilder("someSpan").startSpan();
        span.end();

        getRecordedSpans(1);
    }

    @Test
    public void whenRecordingIsNotEnabled_doNotExportTraces() {
        ElasticTracer tracer = ElasticTracer.create("example");

        doReturn(false).when(Configurations.get(AllInstrumentationConfiguration.class)).isEnabled();

        Span span = tracer.spanBuilder("someSpan").startSpan();
        span.end();

        getRecordedSpans(0);
    }
}
