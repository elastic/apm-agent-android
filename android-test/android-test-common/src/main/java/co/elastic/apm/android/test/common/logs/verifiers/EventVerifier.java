package co.elastic.apm.android.test.common.logs.verifiers;

import static org.junit.Assert.assertEquals;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class EventVerifier extends BaseLogVerifier<EventVerifier> {
    public EventVerifier(LogRecordData log) {
        super(log);
    }

    public EventVerifier isNamed(String eventName) {
        assertEquals(eventName, log.getAttributes().get(AttributeKey.stringKey("event.name")));
        return this;
    }
}
