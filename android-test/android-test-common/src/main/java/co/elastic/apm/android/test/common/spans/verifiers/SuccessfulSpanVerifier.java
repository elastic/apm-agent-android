package co.elastic.apm.android.test.common.spans.verifiers;

import static org.junit.Assert.assertEquals;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;

public class SuccessfulSpanVerifier extends BaseSpanVerifier<SuccessfulSpanVerifier> {
    public SuccessfulSpanVerifier(SpanData span) {
        super(span);
        assertEquals(StatusCode.OK, span.getStatus().getStatusCode());
    }
}
