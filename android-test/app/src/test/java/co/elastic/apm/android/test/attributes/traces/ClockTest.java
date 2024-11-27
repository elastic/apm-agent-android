package co.elastic.apm.android.test.attributes.traces;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import co.elastic.apm.android.sdk.traces.ElasticTracers;
import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.trace.data.SpanData;

public class ClockTest extends BaseRobolectricTest {

    @Test
    public void whenASpanIsCreated_itHasTimestampSetFromElasticClock() {
        long startTimeFromElasticClock = 123456789;
        setNow(startTimeFromElasticClock);
        SpanData span = getSpanData();

        Spans.verify(span)
                .startedAt(startTimeFromElasticClock);
    }

    @Test
    public void whenClockNowChangesInMidSpan_verifyFinalSpanDurationIsNotAffected() {
        long startTimeFromElasticClock = 2_000_000_000;
        doAnswer(invocation -> System.nanoTime()).when(getAgentDependenciesInjector().getElasticClock()).nanoTime();
        setNow(startTimeFromElasticClock);

        Span span = ElasticTracers.androidActivity().spanBuilder("TimeChangeSpan").startSpan();

        // Moving now backwards:
        setNow(1_000_000_000L);

        span.end();

        SpanData recordedSpan = getRecordedSpan();
        Spans.verify(recordedSpan)
                .startedAt(startTimeFromElasticClock);
        assertTrue(recordedSpan.getEndEpochNanos() > startTimeFromElasticClock);
    }

    private void setNow(long now) {
        doReturn(now).when(getAgentDependenciesInjector().getElasticClock()).now();
    }

    private SpanData getSpanData() {
        SpanAttrHost host = new SpanAttrHost();

        host.methodWithSpan();

        List<SpanData> spans = getRecordedSpans(1);
        return spans.get(0);
    }
}
