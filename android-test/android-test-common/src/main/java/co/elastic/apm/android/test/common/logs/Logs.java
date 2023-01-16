package co.elastic.apm.android.test.common.logs;

import co.elastic.apm.android.test.common.logs.verifiers.LogVerifier;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class Logs {

    public static LogVerifier verify(LogRecordData log) {
        return new LogVerifier(log);
    }
}
