package co.elastic.apm.android.test.common.logs.verifiers;

import io.opentelemetry.sdk.logs.data.LogRecordData;

public class LogRecordVerifier extends BaseLogVerifier<LogRecordVerifier> {
    public LogRecordVerifier(LogRecordData log) {
        super(log);
    }
}
