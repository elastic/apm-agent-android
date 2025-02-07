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
package co.elastic.otel.android.internal.services

import android.content.Context
import co.elastic.otel.android.internal.services.appinfo.AppInfoService
import co.elastic.otel.android.internal.services.backgroundwork.BackgroundWorkService
import co.elastic.otel.android.internal.services.network.NetworkService
import co.elastic.otel.android.internal.services.preferences.PreferencesService

internal class ServiceManager private constructor() {
    private val services = mutableMapOf<Class<out Service>, Service>()

    fun getPreferencesService(): PreferencesService {
        return getService(PreferencesService::class.java)
    }

    fun getAppInfoService(): AppInfoService {
        return getService(AppInfoService::class.java)
    }

    fun getNetworkService(): NetworkService {
        return getService(NetworkService::class.java)
    }

    fun getBackgroundWorkService(): BackgroundWorkService {
        return getService(BackgroundWorkService::class.java)
    }

    internal fun close() {
        services.values.forEach { it.stop() }
    }

    private fun start() {
        services.values.forEach { it.start() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <S : Service> getService(type: Class<S>): S {
        return services.getValue(type) as S
    }

    companion object {
        fun create(applicationContext: Context): ServiceManager {
            val manager = ServiceManager()
            manager.services[PreferencesService::class.java] =
                PreferencesService(applicationContext)
            manager.services[AppInfoService::class.java] = AppInfoService(applicationContext)
            manager.services[NetworkService::class.java] =
                NetworkService.create(applicationContext, manager)
            manager.services[BackgroundWorkService::class.java] = BackgroundWorkService.create()

            manager.start()
            return manager
        }
    }
}