package co.elastic.apm.android.test.common.spans.verifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
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

    @Override
    public T hasAttribute(String attributeName) {
        assertNotNull(span.getAttributes().get(AttributeKey.stringKey(attributeName)));
        return (T) this;
    }

    @Override
    public T hasAttribute(String attributeName, String attributeValue) {
        assertEquals(attributeValue, span.getAttributes().get(AttributeKey.stringKey(attributeName)));
        return (T) this;
    }

    @Override
    public T hasResource(String resourceName) {
        assertNotNull(span.getResource().getAttribute(AttributeKey.stringKey(resourceName)));
        return (T) this;
    }

    @Override
    public T hasResource(String resourceName, String resourceValue) {
        assertEquals(resourceValue, span.getResource().getAttribute(AttributeKey.stringKey(resourceName)));
        return (T) this;
    }

    @Override
    public T startedAt(long timeInNanoseconds) {
        assertEquals(timeInNanoseconds, span.getStartEpochNanos());
        return (T) this;
    }

    @Override
    public T isOfKind(SpanKind kind) {
        assertEquals(kind, span.getKind());
        return (T) this;
    }
}
