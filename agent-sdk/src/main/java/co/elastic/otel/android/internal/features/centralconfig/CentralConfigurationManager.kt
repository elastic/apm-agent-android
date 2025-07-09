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

import androidx.annotation.WorkerThread
import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.connectivity.Authentication
import co.elastic.otel.android.internal.connectivity.ConnectivityConfigurationHolder
import co.elastic.otel.android.internal.features.exportergate.ExporterGateManager
import co.elastic.otel.android.internal.opentelemetry.ElasticOpenTelemetry
import co.elastic.otel.android.internal.services.ServiceManager
import java.io.Closeable
import java.io.IOException
import org.slf4j.Logger
import org.stagemonitor.configuration.ConfigurationRegistry

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class CentralConfigurationManager private constructor(
    serviceManager: ServiceManager,
    private val configurationRegistry: ConfigurationRegistry,
    private val centralConfigurationSource: CentralConfigurationSource,
    private val gateManager: ExporterGateManager,
    private val initialParameters: EndpointParameters
) : CentralConfigurationSource.Listener, Closeable {
    private lateinit var centralConnectivityHolder: CentralConnectivityHolder
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }
    private val logger: Logger = Elog.getLogger()

    internal fun initialize(openTelemetry: ElasticOpenTelemetry) {
        centralConnectivityHolder = CentralConnectivityHolder(
            CentralConfigurationConnectivity(
                initialParameters.url,
                initialParameters.auth,
                initialParameters.extraHeaders,
                openTelemetry.serviceName,
                openTelemetry.deploymentEnvironment
            )
        )
        backgroundWorkService.submit {
            try {
                centralConfigurationSource.initialize(getConnectivityConfiguration(), this)
                doPoll()
            } catch (t: Throwable) {
                logger.error("CentralConfiguration initialization error", t)
            } finally {
                openLatch()
            }
        }
    }

    internal fun getCentralConfiguration(): CentralConfiguration {
        return configurationRegistry.getConfig(CentralConfiguration::class.java)
    }

    private fun getConnectivityConfiguration(): CentralConfigurationConnectivity {
        return centralConnectivityHolder.get() as CentralConfigurationConnectivity
    }

    private fun openLatch() {
        gateManager.openLatches(CentralConfigurationManager::class.java)
    }

    @WorkerThread
    @Throws(IOException::class)
    private fun doPoll() {
        val delayForNextPollInSeconds =
            centralConfigurationSource.sync(getConnectivityConfiguration())
        if (delayForNextPollInSeconds != null) {
            logger.info("Central config returned max age is null")
            scheduleInSeconds(delayForNextPollInSeconds)
        } else {
            scheduleDefault()
        }
    }

    companion object {
        internal fun create(
            serviceManager: ServiceManager,
            initialParameters: EndpointParameters,
            gateManager: ExporterGateManager
        ): CentralConfigurationManager {
            val centralConfigurationSource = CentralConfigurationSource(serviceManager)
            val registry = ConfigurationRegistry.builder()
                .addConfigSource(centralConfigurationSource)
                .addOptionProvider(CentralConfiguration())
            val latchName = "Central configuration"
            gateManager.createSpanGateLatch(CentralConfigurationManager::class.java, latchName)
            gateManager.createLogRecordLatch(CentralConfigurationManager::class.java, latchName)
            gateManager.createMetricGateLatch(CentralConfigurationManager::class.java, latchName)
            return CentralConfigurationManager(
                serviceManager,
                registry.build(),
                centralConfigurationSource,
                gateManager,
                initialParameters
            )
        }
    }

    private inner class ConfigurationPoll : Runnable {
        override fun run() {
            try {
                doPoll()
            } catch (t: Throwable) {
                logger.error("Central config poll error", t)
                scheduleDefault()
            }
        }
    }

    override fun onConfigChange() {
        configurationRegistry.reloadDynamicConfigurationOptions()
    }

    internal class CentralConnectivityHolder(connectivity: CentralConfigurationConnectivity) :
        ConnectivityConfigurationHolder(connectivity)

    internal data class EndpointParameters(
        val url: String,
        val auth: Authentication,
        val extraHeaders: Map<String, String>
    )

    override fun close() {
        centralConfigurationSource.close()
    }
}