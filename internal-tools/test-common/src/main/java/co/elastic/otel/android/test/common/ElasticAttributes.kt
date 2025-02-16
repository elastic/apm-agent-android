package co.elastic.otel.android.test.common

import io.opentelemetry.api.common.Attributes

object ElasticAttributes {
    const val DEFAULT_SESSION_ID = "session-id"
    const val DEFAULT_NETWORK_CONNECTION_TYPE = "unavailable"

    fun getLogRecordDefaultAttributes(
        sessionId: String = DEFAULT_SESSION_ID,
        networkConnectionType: String = DEFAULT_NETWORK_CONNECTION_TYPE
    ): Attributes =
        Attributes.builder()
            .put("session.id", sessionId)
            .put("network.connection.type", networkConnectionType)
            .build()

    fun getSpanDefaultAttributes(logRecordAttributes: Attributes = getLogRecordDefaultAttributes()): Attributes =
        Attributes.builder()
            .putAll(logRecordAttributes)
            .put("type", "mobile")
            .build()
}