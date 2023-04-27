package co.elastic.apm.android.test.configuration;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.List;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.sdk.traces.ElasticTracers;
import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.data.SpanData;

public class FilterConfigurationTest extends BaseRobolectricTest {

    @Config(application = SpanFilteredApp.class)
    @Test
    public void verifySpanFilter() {
        Tracer tracer = ElasticTracers.create("someTracer");

        Span spanOne = tracer.spanBuilder("spanOne").startSpan();
        spanOne.setAttribute("includeMe", false);

        Span spanTwo = tracer.spanBuilder("spanTwo").startSpan();
        spanTwo.setAttribute("includeMe", true);

        spanOne.end();
        spanTwo.end();

        List<SpanData> recordedSpans = getRecordedSpans(1);

        Spans.verify(recordedSpans.get(0))
                .isNamed("spanTwo");
    }

    private static class SpanFilteredApp extends BaseRobolectricTestApplication {
        @Override
        public void onCreate() {
            initializeAgentWithCustomConfig(ElasticApmConfiguration.builder()
                    .setSpanFilter(readableSpan -> Boolean.TRUE.equals(readableSpan.getAttribute(AttributeKey.booleanKey("includeMe")))).build());
        }
    }
}
