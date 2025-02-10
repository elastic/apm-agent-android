package co.elastic.otel.android.api.clock

import io.opentelemetry.sdk.common.Clock

interface ClockProvider {
    fun getClock(): Clock
}