package co.elastic.apm.android.test.attributes.traces;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.sdk.traces.ElasticTracers;
import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.TestElasticClock;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.trace.data.SpanData;

public class ClockTest extends BaseRobolectricTest {

    @Test
    public void whenASpanIsCreated_itHasTimestampSetFromElasticClock() {
        long startTimeFromElasticClock = 123456789;
        TestElasticClock clock = (TestElasticClock) getAgentDependenciesInjector().getClock();
        clock.setForcedNow(startTimeFromElasticClock);
        SpanData span = getSpanData();

        Spans.verify(span)
                .startedAt(startTimeFromElasticClock);
    }

    @Test
    public void whenClockNowChangesInMidSpan_verifyFinalSpanDurationIsNotAffected() {
        long startTimeFromElasticClock = 2_000_000_000;
        TestElasticClock clock = (TestElasticClock) getAgentDependenciesInjector().getClock();
        clock.setForcedNow(startTimeFromElasticClock);

        Span span = ElasticTracers.androidActivity().spanBuilder("TimeChangeSpan").startSpan();

        // Moving now backwards:
        clock.setForcedNow(1_000_000_000L);

        span.end();

        SpanData recordedSpan = getRecordedSpan();
        Spans.verify(recordedSpan)
                .startedAt(startTimeFromElasticClock);
        assertTrue(recordedSpan.getEndEpochNanos() > startTimeFromElasticClock);
    }

    private SpanData getSpanData() {
        SpanAttrHost host = new SpanAttrHost();

        host.methodWithSpan();

        List<SpanData> spans = getRecordedSpans(1);
        return spans.get(0);
    }
}
