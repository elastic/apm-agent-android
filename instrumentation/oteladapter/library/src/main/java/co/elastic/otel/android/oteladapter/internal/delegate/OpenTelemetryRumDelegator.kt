package co.elastic.otel.android.oteladapter.internal.delegate

import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.session.SessionProvider
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.sdk.common.Clock

class OpenTelemetryRumDelegator(override val openTelemetry: OpenTelemetry) : OpenTelemetryRum {
    override val sessionProvider: SessionProvider
        get() = SessionProvider.getNoop()
    override val clock: Clock
        get() = Clock.getDefault()
    private val logger: Logger =
        openTelemetry.logsBridge
            .loggerBuilder("io.opentelemetry.rum.events")
            .build()

    override fun emitEvent(
        eventName: String,
        body: String,
        attributes: Attributes
    ) {
        logger.logRecordBuilder()
            .setEventName(eventName)
            .setBody(body)
            .setAllAttributes(attributes)
            .emit()
    }

    override fun shutdown() {
        // no-op
    }
}