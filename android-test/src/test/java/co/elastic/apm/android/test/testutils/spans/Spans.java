package co.elastic.apm.android.test.testutils.spans;

import static org.junit.Assert.assertEquals;

import co.elastic.apm.android.test.testutils.spans.verifiers.FailedSpanVerifier;
import co.elastic.apm.android.test.testutils.spans.verifiers.SuccessfulSpanVerifier;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.SpanData;

public class Spans {

    public static SuccessfulSpanVerifier verify(SpanData span) {
        return new SuccessfulSpanVerifier(span);
    }

    public static FailedSpanVerifier verifyFailed(SpanData spanData) {
        return new FailedSpanVerifier(spanData);
    }

    public static ContextVerifier verify(Context context) {
        return new ContextVerifier(context);
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
