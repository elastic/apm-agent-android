package co.elastic.apm.android.test.testutils.spans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.internal.data.ExceptionEventData;

public class Spans {

    public static SpanVerifier verify(SpanData span) {
        return new SpanVerifier(span);
    }

    public static ContextVerifier verify(Context context) {
        return new ContextVerifier(context);
    }

    public static class SpanVerifier {
        private final SpanData span;

        SpanVerifier(SpanData span) {
            this.span = span;
        }

        public SpanVerifier hasError() {
            assertEquals(StatusCode.ERROR, span.getStatus().getStatusCode());
            return this;
        }

        public SpanVerifier hasRecordedException(Exception e) {
            assertTrue(getRecordedExceptions().contains(e));
            return this;
        }

        public SpanVerifier hasAmountOfRecordedExceptions(int amountOfExceptionsRecorded) {
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

        public SpanVerifier hasNoError() {
            assertNotEquals(StatusCode.ERROR, span.getStatus().getStatusCode());
            return this;
        }

        public SpanVerifier isNamed(String spanMethodName) {
            assertEquals(spanMethodName, span.getName());
            return this;
        }
    }

    public static class ContextVerifier {
        private final Context context;

        ContextVerifier(Context context) {
            this.context = context;
        }

        public ContextVerifier belongsTo(SpanData span) {
            SpanContext spanContext = Span.fromContext(context).getSpanContext();
            assertEquals(spanContext, span.getSpanContext());
            return this;
        }
    }
}
