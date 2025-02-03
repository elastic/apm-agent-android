package co.elastic.otel.android.api.internal

import io.opentelemetry.sdk.common.CompletableResultCode

interface MetricFlusher {
    fun flushMetrics(): CompletableResultCode
}