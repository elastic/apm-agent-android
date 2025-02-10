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
package co.elastic.otel.android.internal.features.centralconfig

import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.internal.features.centralconfig.fetcher.CentralConfigurationFetcher
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.services.preferences.PreferencesService
import co.elastic.otel.android.internal.time.SystemTimeProvider
import com.dslplatform.json.DslJson
import com.dslplatform.json.MapConverter
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.concurrent.TimeUnit
import org.stagemonitor.configuration.source.AbstractConfigurationSource

internal class CentralConfigurationSource internal constructor(
    serviceManager: ServiceManager,
    private val systemTimeProvider: SystemTimeProvider,
) : AbstractConfigurationSource() {
    private val preferences: PreferencesService by lazy { serviceManager.getPreferencesService() }
    private val dslJson = DslJson(DslJson.Settings<Any>())
    private val logger = Elog.getLogger()
    private val configs = mutableMapOf<String, String>()
    private val configFile by lazy {
        File(serviceManager.getAppInfoService().getFilesDir(), "elastic_agent_configuration.json")
    }
    private val fetcher by lazy { CentralConfigurationFetcher(configFile, preferences) }
    internal lateinit var listener: Listener

    companion object {
        private const val REFRESH_TIMEOUT_PREFERENCE_NAME = "central_configuration_refresh_timeout"
    }

    internal fun initialize() {
        loadFromDisk()
        if (configs.isNotEmpty()) {
            notifyListener()
        }
    }

    @Throws(IOException::class)
    internal fun sync(connectivity: CentralConfigurationConnectivity): Int? {
        if (refreshTimeoutMillis > systemTimeProvider.getCurrentTimeMillis()) {
            logger.debug("Ignoring central config sync request")
            return null
        }
        try {
            val fetchResult = fetcher.fetch(connectivity)
            if (fetchResult.configurationHasChanged) {
                loadFromDisk()
                notifyListener()
            }
            val maxAgeInSeconds = fetchResult.maxAgeInSeconds
            if (maxAgeInSeconds != null) {
                storeRefreshTimeoutTime(maxAgeInSeconds)
            }
            return maxAgeInSeconds
        } catch (t: Throwable) {
            logger.error("An error occurred while fetching the central configuration", t)
            throw t
        }
    }

    private fun loadFromDisk() {
        try {
            val configsFromDisk = readConfigsFromDisk()
            configs.clear()
            configs.putAll(configsFromDisk)
        } catch (e: IOException) {
            logger.error("Error wile loading configs from disk", e)
            configs.clear()
        }
    }

    private fun notifyListener() {
        logger.info("Notifying central config change")
        logger.debug("Central config params: {}", configs)
        listener.onConfigChange()
    }

    @Throws(IOException::class)
    private fun readConfigsFromDisk(): Map<String, String> {
        if (!configFile.exists()) {
            return emptyMap()
        }
        val buffer = ByteArray(4096)
        configFile.inputStream().use {
            val reader = dslJson.newReader(it, buffer)
            reader.startObject()
            return Collections.unmodifiableMap(MapConverter.deserialize(reader))
        }
    }

    private fun storeRefreshTimeoutTime(maxAgeInSeconds: Int) {
        logger.debug("Storing central config max age seconds {}", maxAgeInSeconds)
        refreshTimeoutMillis =
            systemTimeProvider.getCurrentTimeMillis() + TimeUnit.SECONDS.toMillis(maxAgeInSeconds.toLong())
    }

    private var refreshTimeoutMillis: Long
        get() = preferences.retrieveLong(REFRESH_TIMEOUT_PREFERENCE_NAME, 0)
        private set(timeoutMillis) {
            preferences.store(REFRESH_TIMEOUT_PREFERENCE_NAME, timeoutMillis)
        }

    override fun getValue(key: String): String? {
        return configs[key]
    }

    override fun getName(): String {
        return "APM Server Central Configuration"
    }

    interface Listener {
        fun onConfigChange()
    }
}
