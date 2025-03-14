/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.otel.android.internal.services.backgroundwork

import co.elastic.otel.android.internal.services.Service
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class BackgroundWorkService private constructor(internal val executorService: ScheduledExecutorService) :
    Service {

    companion object {
        fun create(): BackgroundWorkService {
            return BackgroundWorkService(Executors.newSingleThreadScheduledExecutor())
        }
    }

    internal fun submit(task: Runnable): Future<*> {
        return executorService.submit(task)
    }

    internal fun scheduleOnce(delayMillis: Long, task: Runnable): ScheduledFuture<*>? {
        return executorService.schedule(task, delayMillis, TimeUnit.MILLISECONDS)
    }

    internal fun schedulePeriodicTask(
        delayBetweenExecutions: Long,
        timeUnit: TimeUnit,
        task: Runnable
    ): ScheduledFuture<*>? {
        return schedulePeriodicTask(delayBetweenExecutions, timeUnit, 0, task)
    }

    internal fun schedulePeriodicTask(
        delayBetweenExecutions: Long,
        timeUnit: TimeUnit,
        initialDelay: Long,
        task: Runnable
    ): ScheduledFuture<*>? {
        return executorService.scheduleWithFixedDelay(
            task,
            initialDelay,
            delayBetweenExecutions,
            timeUnit
        )
    }

    override fun stop() {
        executorService.shutdownNow()
    }
}