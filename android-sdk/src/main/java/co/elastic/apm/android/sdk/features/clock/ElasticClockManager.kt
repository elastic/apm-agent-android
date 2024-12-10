package co.elastic.apm.android.sdk.features.clock

import co.elastic.apm.android.sdk.internal.opentelemetry.clock.ElasticClock
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import java.util.concurrent.TimeUnit

internal class ElasticClockManager internal constructor(
    serviceManager: ServiceManager,
    private val clock: ElasticClock
) {
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }

    internal fun initialize() {
        backgroundWorkService.schedulePeriodicTask(SyncHandler(), 1, TimeUnit.MINUTES)
    }

    private inner class SyncHandler : Runnable {
        override fun run() {
            clock.sync()
        }
    }
}