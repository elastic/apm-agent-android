package co.elastic.apm.android.sdk.logs;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;

public final class ElasticLogRecordProcessor implements LogRecordProcessor {
    private final LogRecordProcessor original;

    public ElasticLogRecordProcessor(LogRecordProcessor original) {
        this.original = original;
    }

    @Override
    public void onEmit(Context context, ReadWriteLogRecord logRecord) {
        original.onEmit(context, logRecord);
    }

    @Override
    public CompletableResultCode shutdown() {
        return original.shutdown();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return original.forceFlush();
    }

    @Override
    public void close() {
        original.close();
    }
}
