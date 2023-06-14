package co.elastic.apm.android.test.common.logs.verifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.logs.data.LogRecordData;

@SuppressWarnings("unchecked")
public abstract class BaseLogVerifier<T extends LogVerifier<?>> implements LogVerifier<T> {
    protected final LogRecordData log;

    public BaseLogVerifier(LogRecordData log) {
        this.log = log;
    }

    @Override
    public T hasResource(String resourceName) {
        assertNotNull(log.getResource().getAttribute(AttributeKey.stringKey(resourceName)));
        return (T) this;
    }

    @Override
    public T hasResource(String resourceName, String resourceValue) {
        assertEquals(resourceValue, log.getResource().getAttribute(AttributeKey.stringKey(resourceName)));
        return (T) this;
    }

    @Override
    public T hasResource(String resourceName, Integer resourceValue) {
        assertEquals(Long.valueOf(resourceValue), log.getResource().getAttribute(AttributeKey.longKey(resourceName)));
        return (T) this;
    }

    @Override
    public T startedAt(long timeInNanoseconds) {
        assertEquals(timeInNanoseconds, log.getObservedTimestampEpochNanos());
        return (T) this;
    }

    @Override
    public T hasAttribute(String attrName) {
        assertNotNull(log.getAttributes().get(AttributeKey.stringKey(attrName)));
        return (T) this;
    }

    @Override
    public T hasAttribute(String attrName, String attrValue) {
        assertEquals(attrValue, log.getAttributes().get(AttributeKey.stringKey(attrName)));
        return (T) this;
    }

    @Override
    public T hasBody(String body) {
        assertEquals(body, log.getBody().asString());
        return (T) this;
    }
}
