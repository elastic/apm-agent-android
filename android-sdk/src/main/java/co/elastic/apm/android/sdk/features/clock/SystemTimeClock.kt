package co.elastic.apm.android.sdk.features.clock

import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import io.opentelemetry.sdk.common.Clock
import java.util.concurrent.TimeUnit

internal class SystemTimeClock(private val systemTimeProvider: SystemTimeProvider) : Clock {

    override fun now(): Long {
        return TimeUnit.MILLISECONDS.toNanos(systemTimeProvider.getCurrentTimeMillis())
    }

    override fun nanoTime(): Long {
        return systemTimeProvider.getNanoTime()
    }
}