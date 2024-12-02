package co.elastic.otel.api

import io.opentelemetry.api.OpenTelemetry

interface ElasticAgent {
    fun getOpenTelemetry(): OpenTelemetry
}