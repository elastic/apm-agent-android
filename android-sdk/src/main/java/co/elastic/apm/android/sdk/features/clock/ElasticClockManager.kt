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