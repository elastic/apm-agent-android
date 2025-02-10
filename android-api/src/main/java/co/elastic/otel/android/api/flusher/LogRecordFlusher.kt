package co.elastic.otel.android.api.flusher

import io.opentelemetry.sdk.common.CompletableResultCode

interface LogRecordFlusher {
    fun flushLogRecords(): CompletableResultCode
}