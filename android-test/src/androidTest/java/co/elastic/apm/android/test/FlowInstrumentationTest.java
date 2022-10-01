package co.elastic.apm.android.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import co.elastic.apm.android.test.activities.StuckCoroutineActivity;
import co.elastic.apm.android.test.base.BaseEspressoTest;
import co.elastic.apm.android.test.common.spans.Spans;
import io.opentelemetry.sdk.trace.data.SpanData;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FlowInstrumentationTest extends BaseEspressoTest<StuckCoroutineActivity> {

    @Test
    public void onCreation_whenCoroutineGetsStuck_discardAndRevertContextHistory() {
        List<SpanData> spans = getRecordedSpans(2);

        SpanData rootSpan = spans.get(0);
        SpanData onCreateSpan = spans.get(1);

        Spans.verify(onCreateSpan)
                .isDirectChildOf(rootSpan);
    }

    @Override
    protected Class<StuckCoroutineActivity> getActivityClass() {
        return StuckCoroutineActivity.class;
    }
}
