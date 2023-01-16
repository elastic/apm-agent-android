package co.elastic.apm.android.test.common.logs.verifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class LogVerifier {
    private final LogRecordData log;

    public LogVerifier(LogRecordData log) {
        this.log = log;
    }

    public LogVerifier hasResource(String resourceName) {
        assertNotNull(log.getResource().getAttribute(AttributeKey.stringKey(resourceName)));
        return this;
    }

    public LogVerifier hasResource(String resourceName, String resourceValue) {
        assertEquals(resourceValue, log.getResource().getAttribute(AttributeKey.stringKey(resourceName)));
        return this;
    }

    public LogVerifier startedAt(long timeInNanoseconds) {
        assertEquals(timeInNanoseconds, log.getEpochNanos());
        return this;
    }

    public LogVerifier hasAttribute(String attrName) {
        assertNotNull(log.getAttributes().get(AttributeKey.stringKey(attrName)));
        return this;
    }

    public LogVerifier hasAttribute(String attrName, String attrValue) {
        assertEquals(attrValue, log.getAttributes().get(AttributeKey.stringKey(attrName)));
        return this;
    }
}
