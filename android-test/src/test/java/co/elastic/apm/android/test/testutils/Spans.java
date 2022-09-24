package co.elastic.apm.android.test.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.SpanData;

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
