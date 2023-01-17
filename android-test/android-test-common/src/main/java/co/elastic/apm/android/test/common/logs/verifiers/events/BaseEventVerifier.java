package co.elastic.apm.android.test.common.logs.verifiers.events;

import co.elastic.apm.android.test.common.logs.verifiers.BaseLogVerifier;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class BaseEventVerifier<T extends EventVerifier<?>> extends BaseLogVerifier<T> implements EventVerifier<T> {

    public BaseEventVerifier(LogRecordData log) {
        super(log);
        hasAttribute("event.domain", "device");
    }

    @Override
    public T isNamed(String eventName) {
        return hasAttribute("event.name", eventName);
    }
}