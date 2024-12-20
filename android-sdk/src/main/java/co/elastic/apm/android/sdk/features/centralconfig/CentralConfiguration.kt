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
package co.elastic.apm.android.sdk.features.centralconfig

import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.connectivity.ConnectivityConfigurationHolder
import co.elastic.apm.android.sdk.features.centralconfig.fetcher.CentralConfigurationFetcher
import co.elastic.apm.android.sdk.internal.configuration.Configurations
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.internal.services.kotlin.appinfo.AppInfoService
import co.elastic.apm.android.sdk.internal.services.kotlin.preferences.PreferencesService
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import com.dslplatform.json.DslJson
import com.dslplatform.json.MapConverter
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.concurrent.TimeUnit
import org.slf4j.Logger
import org.stagemonitor.configuration.source.AbstractConfigurationSource

class CentralConfiguration internal constructor(
    serviceManager: ServiceManager,
    private val connectivityConfigurationHolder: ConnectivityConfigurationHolder,
    private val systemTimeProvider: SystemTimeProvider
) : AbstractConfigurationSource() {
    private val appInfoService: AppInfoService by lazy { serviceManager.getAppInfoService() }
    private val preferences: PreferencesService by lazy { serviceManager.getPreferencesService() }
    private val dslJson = DslJson(DslJson.Settings<Any>())
    private val logger: Logger = Elog.getLogger()
    private val configs: MutableMap<String, String> = mutableMapOf()
    private val fetcher by lazy { CentralConfigurationFetcher(::getConfigurationFile, preferences) }

    @Volatile
    private var configFile: File? = null

    companion object {
        private const val REFRESH_TIMEOUT_PREFERENCE_NAME = "central_configuration_refresh_timeout"

        internal fun create(
            serviceManager: ServiceManager,
            connectivityConfigurationHolder: ConnectivityConfigurationHolder
        ): CentralConfiguration {
            return CentralConfiguration(
                serviceManager,
                connectivityConfigurationHolder,
                SystemTimeProvider.get()
            )
        }
    }

    internal fun publishCachedConfig() {
        val configurationFile = getConfigurationFile()
        if (!configurationFile.exists()) {
            logger.debug("No cached central config found")
            return
        }
        try {
            notifyListeners()
        } catch (t: Throwable) {
            logger.error("Exception when publishing cached central config", t)
        }
    }

    @Throws(IOException::class)
    internal fun sync(): Int? {
        if (refreshTimeoutMillis > systemTimeProvider.getCurrentTimeMillis()) {
            logger.debug("Ignoring central config sync request")
            return null
        }
        try {
            val fetchResult = fetcher.fetch(connectivityConfigurationHolder.get())
            if (fetchResult.configurationHasChanged) {
                notifyListeners()
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

    @Throws(IOException::class)
    private fun notifyListeners() {
        try {
            configs.putAll(readConfigs(getConfigurationFile()))
            logger.info("Notifying central config change")
            logger.debug("Central config params: {}", configs)
            Configurations.reload()
        } finally {
            configs.clear()
        }
    }

    @Throws(IOException::class)
    private fun readConfigs(configFile: File): Map<String, String> {
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

    @Synchronized
    fun getConfigurationFile(): File {
        if (configFile == null) {
            configFile = File(appInfoService.getFilesDir(), "elastic_agent_configuration.json")
        }
        return configFile!!
    }

    override fun getValue(key: String): String? {
        return configs[key]
    }

    override fun getName(): String {
        return "APM Server"
    }
}
