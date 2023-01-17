package co.elastic.apm.android.test.common.logs;

import co.elastic.apm.android.test.common.logs.verifiers.LogRecordVerifier;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class Logs {

    public static LogRecordVerifier verify(LogRecordData log) {
        return new LogRecordVerifier(log);
    }
}
