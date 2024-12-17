package co.elastic.apm.android.sdk.features.clock

import io.opentelemetry.sdk.common.Clock
import java.util.concurrent.atomic.AtomicReference

internal class MutableClock(initialClock: Clock) : Clock {
    private val delegate: AtomicReference<Clock> = AtomicReference(initialClock)

    override fun now(): Long {
        return delegate.get().now()
    }

    override fun nanoTime(): Long {
        return delegate.get().nanoTime()
    }

    internal fun setDelegate(clock: Clock) {
        delegate.set(clock)
    }
}