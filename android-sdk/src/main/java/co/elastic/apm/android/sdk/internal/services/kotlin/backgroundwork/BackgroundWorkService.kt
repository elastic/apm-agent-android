package co.elastic.apm.android.sdk.internal.services.kotlin.backgroundwork

import co.elastic.apm.android.sdk.internal.services.kotlin.Service
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class BackgroundWorkService private constructor(private val executorService: ScheduledExecutorService) :
    Service {

    companion object {
        fun create(): BackgroundWorkService {
            return BackgroundWorkService(Executors.newSingleThreadScheduledExecutor())
        }
    }

    internal fun submit(task: Runnable): Future<*> {
        return executorService.submit(task)
    }

    internal fun schedule(task: Runnable, seconds: Int): ScheduledFuture<*>? {
        return executorService.schedule(task, seconds.toLong(), TimeUnit.SECONDS)
    }

    override fun stop() {
        executorService.shutdownNow()
    }
}