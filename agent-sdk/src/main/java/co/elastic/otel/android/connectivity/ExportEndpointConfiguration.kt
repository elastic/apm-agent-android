package co.elastic.otel.android.connectivity

import co.elastic.otel.android.exporters.configuration.ExportProtocol

data class ExportEndpointConfiguration(
    val url: String,
    val authentication: Authentication,
    val protocol: ExportProtocol
)