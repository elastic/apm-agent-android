package co.elastic.apm.android.test.attributes.traces;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.TestElasticClock;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.trace.data.SpanData;

public class ClockTest extends BaseRobolectricTest {

    @Test
    public void whenASpanIsCreated_itHasTimestampSetFromElasticClock() {
        long startTimeFromElasticClock = 123456789;
        NtpManager ntpManager = getAgentDependenciesInjector().getNtpManager();
        TestElasticClock clock = (TestElasticClock) ntpManager.getClock();
        clock.setForcedNow(startTimeFromElasticClock);
        SpanData span = getSpanData();

        Spans.verify(span)
                .startedAt(startTimeFromElasticClock);
    }

    @Test
    public void whenClockNowChangesInMidSpan_verifyFinalSpanDurationIsNotAffected() {
        long startTimeFromElasticClock = 2_000_000_000;
        NtpManager ntpManager = getAgentDependenciesInjector().getNtpManager();
        TestElasticClock clock = (TestElasticClock) ntpManager.getClock();
        clock.setForcedNow(startTimeFromElasticClock);

        Span span = ElasticTracer.androidActivity().spanBuilder("TimeChangeSpan").startSpan();

        // Moving now backwards:
        clock.setForcedNow(1_000_000_000L);

        span.end();

        SpanData recordedSpan = getRecordedSpan();
        Spans.verify(recordedSpan)
                .startedAt(startTimeFromElasticClock);
        assertTrue(recordedSpan.getEndEpochNanos() > startTimeFromElasticClock);
    }

    @Test
    public void whenASpanIsCreated_itHasInitializedTheNtpManager() {
        verify(getAgentDependenciesInjector().getNtpManager()).initialize();
    }

    private SpanData getSpanData() {
        SpanAttrHost host = new SpanAttrHost();

        host.methodWithSpan();

        List<SpanData> spans = getRecordedSpans(1);
        return spans.get(0);
    }
}
