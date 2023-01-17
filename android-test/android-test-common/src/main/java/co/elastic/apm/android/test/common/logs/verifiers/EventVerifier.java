package co.elastic.apm.android.test.common.logs.verifiers;

import io.opentelemetry.sdk.logs.data.LogRecordData;

public class EventVerifier extends BaseLogVerifier<EventVerifier> {
    public EventVerifier(LogRecordData log) {
        super(log);
        hasAttribute("event.domain", "device");
    }

    public EventVerifier isNamed(String eventName) {
        return hasAttribute("event.name", eventName);
    }
}
