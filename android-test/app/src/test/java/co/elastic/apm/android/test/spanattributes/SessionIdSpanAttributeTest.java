package co.elastic.apm.android.test.spanattributes;

import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.trace.data.SpanData;

public class SessionIdSpanAttributeTest extends BaseRobolectricTest {

    @Test
    public void whenASpanIsCreated_verifyItHasSessionIdAsParam() {
        SpanAttrHost host = new SpanAttrHost();

        host.methodWithSpan();

        List<SpanData> spans = getRecordedSpans(1);
        SpanData customSpan = spans.get(0);

        Spans.verify(customSpan)
                .hasAttributeNamed("session.id");
    }
}
