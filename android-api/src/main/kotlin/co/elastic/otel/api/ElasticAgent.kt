package co.elastic.otel.api

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.common.CompletableResultCode

interface ElasticAgent {
    fun getOpenTelemetry(): OpenTelemetry

    fun flushLogRecords(): CompletableResultCode
}