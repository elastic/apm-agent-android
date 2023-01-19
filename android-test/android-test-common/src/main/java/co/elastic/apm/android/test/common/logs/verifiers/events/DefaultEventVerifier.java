package co.elastic.apm.android.test.common.logs.verifiers.events;

import io.opentelemetry.sdk.logs.data.LogRecordData;

public class DefaultEventVerifier extends BaseEventVerifier<DefaultEventVerifier> {
    public DefaultEventVerifier(LogRecordData log) {
        super(log);
    }
}
