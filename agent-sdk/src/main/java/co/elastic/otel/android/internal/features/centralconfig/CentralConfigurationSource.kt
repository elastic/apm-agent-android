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
import co.elastic.otel.android.internal.opamp.OpampClient
import co.elastic.otel.android.internal.opamp.connectivity.http.OkHttpSender
import co.elastic.otel.android.internal.opamp.request.service.HttpRequestService
import co.elastic.otel.android.internal.opamp.response.MessageData
import co.elastic.otel.android.internal.services.ServiceManager
import com.dslplatform.json.DslJson
import com.dslplatform.json.MapConverter
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.Collections
import okio.ByteString
import opamp.proto.AgentConfigFile
import opamp.proto.RemoteConfigStatus
import opamp.proto.RemoteConfigStatuses
import opamp.proto.ServerErrorResponse
import org.stagemonitor.configuration.source.AbstractConfigurationSource


/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class CentralConfigurationSource internal constructor(
    serviceManager: ServiceManager
) : AbstractConfigurationSource(), OpampClient.Callbacks, Closeable {
    private val dslJson = DslJson(DslJson.Settings<Any>())
    private val logger = Elog.getLogger()
    private val configs = mutableMapOf<String, String>()
    private val configFile by lazy {
        File(serviceManager.getAppInfoService().getFilesDir(), "elastic_agent_configuration.json")
    }
    private lateinit var listener: Listener
    private lateinit var opampClient: OpampClient

    internal fun initialize(connectivity: CentralConfigurationConnectivity, listener: Listener) {
        this.opampClient = createOpampClient(connectivity)
        this.listener = listener
        opampClient.start(this)
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

    private fun createOpampClient(connectivity: CentralConfigurationConnectivity): OpampClient {
        val builder = OpampClient.builder()
            .enableRemoteConfig()
            .setServiceName(connectivity.serviceName)
        connectivity.serviceDeployment?.let {
            builder.setDeploymentEnvironmentName(it)
        }
        builder.setRequestService(HttpRequestService.create(OkHttpSender.create(connectivity.getUrl())))
        return builder.build()
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

    override fun getValue(key: String): String? {
        return configs[key]
    }

    override fun getName(): String {
        return "APM Server Central Configuration"
    }

    interface Listener {
        fun onConfigChange()
    }

    override fun onConnect(client: OpampClient) {
        logger.debug("OpAMP connected successfully")
    }

    override fun onConnectFailed(client: OpampClient, throwable: Throwable?) {
        logger.debug("OpAMP connection failed", throwable)
    }

    override fun onErrorResponse(client: OpampClient, errorResponse: ServerErrorResponse) {
        logger.debug("OpAMP failed response: {}", errorResponse)
    }

    override fun onMessage(client: OpampClient, messageData: MessageData) {
        logger.debug("OpAMP on message: {}", messageData)
        val remoteConfig = messageData.remoteConfig ?: return

        val elasticConfig = remoteConfig.config?.config_map?.get("elastic")
        logger.debug("OpAMP elastic config contents: {}", elasticConfig)

        val status = if (elasticConfig != null) {
            if (storeConfig(elasticConfig)) {
                loadFromDisk()
                notifyListener()
                RemoteConfigStatuses.RemoteConfigStatuses_APPLIED
            } else {
                RemoteConfigStatuses.RemoteConfigStatuses_FAILED
            }
        } else {
            RemoteConfigStatuses.RemoteConfigStatuses_FAILED
        }

        logger.debug(
            "OpAMP sending remote config satus: {}, with hash: {}",
            status,
            remoteConfig.config_hash
        )
        opampClient.setRemoteConfigStatus(getRemoteConfigStatus(status, remoteConfig.config_hash))
    }

    private fun storeConfig(elasticConfig: AgentConfigFile): Boolean {
        try {
            configFile.outputStream().use {
                elasticConfig.body.write(it)
            }
            return true
        } catch (e: IOException) {
            logger.error("OpAMP error while storing config", e)
        }
        return false
    }

    private fun getRemoteConfigStatus(
        status: RemoteConfigStatuses, hash: ByteString?
    ): RemoteConfigStatus {
        val builder = RemoteConfigStatus.Builder()
            .status(status)
        if (hash != null && status == RemoteConfigStatuses.RemoteConfigStatuses_APPLIED) {
            builder.last_remote_config_hash(hash)
        }
        return builder.build()
    }

    override fun close() {
        opampClient.stop()
    }
}
