package co.elastic.apm.android.test.common.logs;

import co.elastic.apm.android.test.common.logs.verifiers.LogRecordVerifier;
import co.elastic.apm.android.test.common.logs.verifiers.events.DefaultEventVerifier;
import co.elastic.apm.android.test.common.logs.verifiers.events.exceptions.ExceptionEventVerifier;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class Logs {

    public static LogRecordVerifier verifyRecord(LogRecordData log) {
        return new LogRecordVerifier(log);
    }

    public static DefaultEventVerifier verifyEvent(LogRecordData event) {
        return new DefaultEventVerifier(event);
    }

    public static ExceptionEventVerifier verifyException(LogRecordData exceptionEvent) {
        return new ExceptionEventVerifier(exceptionEvent);
    }
}
