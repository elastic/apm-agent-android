package co.elastic.apm.android.test.common.logs.verifiers.events.exceptions;

import static org.junit.Assert.assertEquals;

import co.elastic.apm.android.test.common.logs.verifiers.events.BaseEventVerifier;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class ExceptionEventVerifier extends BaseEventVerifier<ExceptionEventVerifier> {
    public ExceptionEventVerifier(LogRecordData log) {
        super(log);
    }

    public ExceptionEventVerifier hasExceptionMessage(String message) {
        assertEquals(message, log.getAttributes().get(AttributeKey.stringKey("exception.message")));
        return this;
    }

    public ExceptionEventVerifier hasStacktrace(String stacktrace) {
        assertEquals(stacktrace, log.getAttributes().get(AttributeKey.stringKey("exception.stacktrace")));
        return this;
    }

    public ExceptionEventVerifier hasExceptionType(String typeName) {
        assertEquals(typeName, log.getAttributes().get(AttributeKey.stringKey("exception.type")));
        return this;
    }
}
