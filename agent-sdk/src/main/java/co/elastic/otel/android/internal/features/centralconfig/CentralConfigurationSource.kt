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
import co.elastic.otel.android.internal.opentelemetry.ElasticOpenTelemetry
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
    private var requestService: HttpRequestService? = null

    internal fun initialize(
        connectivity: CentralConfigurationConnectivity,
        openTelemetry: ElasticOpenTelemetry,
        listener: Listener
    ) {
        this.opampClient = createOpampClient(connectivity, openTelemetry)
        this.listener = listener
        opampClient.start(this)
        loadFromDisk()
        if (configs.isNotEmpty()) {
            notifyListener()
        }
    }

    internal fun forceSync() {
        requestService?.sendRequest()
    }

    private fun createOpampClient(
        connectivity: CentralConfigurationConnectivity,
        openTelemetry: ElasticOpenTelemetry
    ): OpampClient {
        val builder = OpampClient.builder()
            .enableRemoteConfig()
            .setServiceName(openTelemetry.serviceName)
        openTelemetry.deploymentEnvironment?.let {
            builder.setDeploymentEnvironmentName(it)
        }
        requestService = HttpRequestService.create(OkHttpSender.create(connectivity.getUrl()))
        builder.setRequestService(requestService)
        return builder.build()
    }

    private fun loadFromDisk(): Boolean {
        try {
            val configsFromDisk = readConfigsFromDisk()
            configs.clear()
            configs.putAll(configsFromDisk)
            return true
        } catch (e: IOException) {
            logger.error("Error wile loading configs from disk", e)
            configs.clear()
        }
        return false
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
            if (storeConfig(elasticConfig) && loadFromDisk()) {
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
        client.setRemoteConfigStatus(getRemoteConfigStatus(status, remoteConfig.config_hash))
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
        if (hash != null) {
            builder.last_remote_config_hash(hash)
        }
        return builder.build()
    }

    override fun close() {
        opampClient.stop()
    }
}
