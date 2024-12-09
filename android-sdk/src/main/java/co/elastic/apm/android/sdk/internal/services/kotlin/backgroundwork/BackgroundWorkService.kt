package co.elastic.apm.android.sdk.internal.services.kotlin.backgroundwork

import co.elastic.apm.android.sdk.internal.services.kotlin.Service
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class BackgroundWorkService private constructor(private val executorService: ScheduledExecutorService) :
    Service {

    companion object {
        fun create(): BackgroundWorkService {
            return BackgroundWorkService(Executors.newSingleThreadScheduledExecutor())
        }
    }

    override fun stop() {
        executorService.shutdownNow()
    }
}