package co.elastic.apm.android.test.testutils.spans.verifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.internal.data.ExceptionEventData;

public class FailedSpanVerifier extends BaseSpanVerifier<FailedSpanVerifier> {

    public FailedSpanVerifier(SpanData span) {
        super(span);
        assertEquals(StatusCode.ERROR, span.getStatus().getStatusCode());
    }

    public FailedSpanVerifier hasRecordedException(Exception e) {
        assertTrue(getRecordedExceptions().contains(e));
        return this;
    }

    public FailedSpanVerifier hasAmountOfRecordedExceptions(int amountOfExceptionsRecorded) {
        assertEquals(amountOfExceptionsRecorded, getRecordedExceptions().size());
        return this;
    }

    private List<Throwable> getRecordedExceptions() {
        List<EventData> events = span.getEvents();
        List<Throwable> exceptions = new ArrayList<>();

        for (EventData event : events) {
            if (event instanceof ExceptionEventData) {
                ExceptionEventData eventData = (ExceptionEventData) event;
                exceptions.add(eventData.getException());
            }
        }

        return exceptions;
    }
}
