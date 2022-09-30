package co.elastic.apm.android.test.spans.verifiers;

import static org.junit.Assert.assertNotEquals;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;

public class SuccessfulSpanVerifier extends BaseSpanVerifier<SuccessfulSpanVerifier> {
    public SuccessfulSpanVerifier(SpanData span) {
        super(span);
        assertNotEquals(StatusCode.ERROR, span.getStatus().getStatusCode());
    }
}
