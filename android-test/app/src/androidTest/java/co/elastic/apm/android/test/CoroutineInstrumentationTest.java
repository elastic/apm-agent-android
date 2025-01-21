package co.elastic.apm.android.test;

import androidx.test.filters.LargeTest;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.test.activities.CoroutineActivity;
import co.elastic.apm.android.test.base.ActivityEspressoTest;
import co.elastic.apm.android.test.common.spans.Spans;
import io.opentelemetry.sdk.trace.data.SpanData;

@Ignore("Not yet implemented")
@LargeTest
public class CoroutineInstrumentationTest extends ActivityEspressoTest<CoroutineActivity> {

    @Test
    public void onCreation_whenCoroutineGetsLaunched_propagateCurrentSpanContext() {
        List<SpanData> spans = getRecordedSpans(7);

        SpanData onCreateSpan = spans.get(0);
        SpanData myCoroutineSpan = spans.get(1);

        Spans.verify(onCreateSpan)
                .hasNoParent();

        Spans.verify(myCoroutineSpan)
                .isNamed("My span inside a coroutine")
                .isDirectChildOf(onCreateSpan);
    }

    private List<SpanData> getRecordedSpans(int i) {
        return null;
    }

    @Override
    protected Class<CoroutineActivity> getActivityClass() {
        return CoroutineActivity.class;
    }
}
