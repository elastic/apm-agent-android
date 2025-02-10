package co.elastic.otel.android.api.flusher

import io.opentelemetry.sdk.common.CompletableResultCode

interface MetricFlusher {
    fun flushMetrics(): CompletableResultCode
}