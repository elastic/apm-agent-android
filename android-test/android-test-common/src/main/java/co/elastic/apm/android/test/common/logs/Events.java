package co.elastic.apm.android.test.common.logs;

import co.elastic.apm.android.test.common.logs.verifiers.EventVerifier;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class Events {

    public static EventVerifier verify(LogRecordData event) {
        return new EventVerifier(event);
    }
}
