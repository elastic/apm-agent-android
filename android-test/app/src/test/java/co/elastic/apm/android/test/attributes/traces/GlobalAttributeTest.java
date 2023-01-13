package co.elastic.apm.android.test.attributes.traces;

import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.trace.data.SpanData;

public class GlobalAttributeTest extends BaseRobolectricTest {

    @Test
    public void whenASpanIsCreated_verifyItHasSessionIdAsParam() {
        SpanData customSpan = getSpanData();

        Spans.verify(customSpan)
                .hasAttribute("session.id");
    }

    @Test
    public void whenASpanIsCreated_verifyItHasTypeMobileAsParam() {
        SpanData customSpan = getSpanData();

        Spans.verify(customSpan)
                .hasAttribute("type", "mobile");
    }

    private SpanData getSpanData() {
        SpanAttrHost host = new SpanAttrHost();

        host.methodWithSpan();

        List<SpanData> spans = getRecordedSpans(1);
        return spans.get(0);
    }
}
