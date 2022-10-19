package co.elastic.apm.android.test.common.spans.verifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import io.opentelemetry.sdk.trace.data.SpanData;

@SuppressWarnings("unchecked")
public abstract class BaseSpanVerifier<T extends SpanVerifier<?>> implements SpanVerifier<T> {
    protected final SpanData span;

    protected BaseSpanVerifier(SpanData span) {
        this.span = span;
    }

    @Override
    public T isNamed(String spanName) {
        assertEquals(spanName, span.getName());
        return (T) this;
    }

    @Override
    public T isDirectChildOf(SpanData span) {
        assertEquals(span.getSpanId(), this.span.getParentSpanId());
        return (T) this;
    }

    @Override
    public T hasNoParent() {
        assertFalse(span.getParentSpanContext().isValid());
        return (T) this;
    }
}
