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
package co.elastic.apm.android.sdk.internal.services.re

import android.app.Application
import co.elastic.apm.android.sdk.internal.services.re.appinfo.AppInfoService
import co.elastic.apm.android.sdk.internal.services.re.preferences.PreferencesService
import java.io.Closeable

class ServiceManager(private val services: Map<Class<out Service>, Service>) : Closeable {

    init {
        services.values.forEach { it.start() }
    }

    fun getPreferencesService(): PreferencesService {
        return getService(PreferencesService::class.java)
    }

    fun getAppInfoService(): AppInfoService {
        return getService(AppInfoService::class.java)
    }

    override fun close() {
        services.values.forEach { it.stop() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <S : Service> getService(type: Class<S>): S {
        return services.getValue(type) as S
    }

    companion object {
        fun create(application: Application): ServiceManager {
            val services = listOf(
                PreferencesService(application),
                AppInfoService(application)
            )
            return ServiceManager(services.associateBy { it.javaClass })
        }
    }
}