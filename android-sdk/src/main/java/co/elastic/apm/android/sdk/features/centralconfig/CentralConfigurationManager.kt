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

import androidx.annotation.WorkerThread
import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.connectivity.ConnectivityConfigurationHolder
import co.elastic.apm.android.sdk.features.apmserver.ApmServerConnectivityManager
import co.elastic.apm.android.sdk.features.exportergate.ExporterGateManager
import co.elastic.apm.android.sdk.internal.services.ServiceManager
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import java.io.IOException
import java.util.concurrent.TimeUnit
import org.slf4j.Logger
import org.stagemonitor.configuration.ConfigurationRegistry

class CentralConfigurationManager private constructor(
    serviceManager: ServiceManager,
    private val configurationRegistry: ConfigurationRegistry,
    private val centralConfigurationSource: CentralConfigurationSource,
    private val connectivityHolder: ConnectivityHolder,
    private val gateManager: ExporterGateManager
) : CentralConfigurationSource.Listener {
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }
    private val logger: Logger = Elog.getLogger()

    fun getConnectivityConfiguration(): CentralConfigurationConnectivity {
        return connectivityHolder.get() as CentralConfigurationConnectivity
    }

    fun setConnectivityConfiguration(configuration: CentralConfigurationConnectivity) {
        connectivityHolder.unlinkFromExportersConfig()
        connectivityHolder.set(configuration)
    }

    internal fun initialize() {
        backgroundWorkService.submit {
            try {
                centralConfigurationSource.initialize()
                openLatch()
                doPoll()
            } catch (t: Throwable) {
                logger.error("CentralConfiguration initialization error", t)
                scheduleDefault()
            } finally {
                openLatch()
            }
        }
    }

    internal fun getCentralConfiguration(): CentralConfiguration {
        return configurationRegistry.getConfig(CentralConfiguration::class.java)
    }

    private fun openLatch() {
        gateManager.openLatches(CentralConfigurationManager::class.java)
    }

    private fun scheduleDefault() {
        scheduleInSeconds(DEFAULT_POLLING_INTERVAL_IN_SECONDS)
    }

    private fun scheduleInSeconds(seconds: Int) {
        backgroundWorkService.scheduleOnce(
            TimeUnit.SECONDS.toMillis(seconds.toLong()),
            ConfigurationPoll()
        )
    }

    @WorkerThread
    @Throws(IOException::class)
    private fun doPoll() {
        val delayForNextPollInSeconds = centralConfigurationSource.sync()
        if (delayForNextPollInSeconds != null) {
            logger.info("Central config returned max age is null")
            scheduleInSeconds(delayForNextPollInSeconds)
        } else {
            scheduleDefault()
        }
    }

    companion object {
        private const val DEFAULT_POLLING_INTERVAL_IN_SECONDS = 60

        internal fun create(
            serviceManager: ServiceManager,
            systemTimeProvider: SystemTimeProvider,
            gateManager: ExporterGateManager,
            serviceName: String,
            serviceDeployment: String?,
            connectivityHolder: ApmServerConnectivityManager.ConnectivityHolder
        ): CentralConfigurationManager {
            val centralConfigurationConnectivityHolder = ConnectivityHolder.fromApmServerConfig(
                serviceName, serviceDeployment, connectivityHolder
            )
            val centralConfigurationSource = CentralConfigurationSource(
                serviceManager,
                centralConfigurationConnectivityHolder,
                systemTimeProvider
            )
            val registry = ConfigurationRegistry.builder()
                .addConfigSource(centralConfigurationSource)
                .addOptionProvider(CentralConfiguration())
            val latchName = "Central configuration"
            gateManager.createSpanGateLatch(CentralConfigurationManager::class.java, latchName)
            gateManager.createLogRecordLatch(CentralConfigurationManager::class.java, latchName)
            gateManager.createMetricGateLatch(CentralConfigurationManager::class.java, latchName)
            val centralConfigurationManager = CentralConfigurationManager(
                serviceManager,
                registry.build(),
                centralConfigurationSource,
                centralConfigurationConnectivityHolder,
                gateManager
            )
            centralConfigurationSource.listener = centralConfigurationManager
            return centralConfigurationManager
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

    internal class ConnectivityHolder(
        connectivity: CentralConfigurationConnectivity,
        private val apmServerConnectivityHolder: ApmServerConnectivityManager.ConnectivityHolder,
        private val serviceName: String,
        private val serviceDeployment: String?
    ) : ConnectivityConfigurationHolder(connectivity), ConnectivityConfigurationHolder.Listener {

        companion object {
            fun fromApmServerConfig(
                serviceName: String,
                serviceDeployment: String?,
                apmServerConfigurationManager: ApmServerConnectivityManager.ConnectivityHolder
            ): ConnectivityHolder {
                val instance = ConnectivityHolder(
                    CentralConfigurationConnectivity.fromApmServerConfig(
                        serviceName,
                        serviceDeployment,
                        apmServerConfigurationManager.getConnectivityConfiguration()
                    ), apmServerConfigurationManager, serviceName, serviceDeployment
                )
                apmServerConfigurationManager.addListener(instance)
                return instance
            }
        }

        internal fun unlinkFromExportersConfig() {
            apmServerConnectivityHolder.removeListener(this)
        }

        override fun onConnectivityConfigurationChange() {
            // On apm server exporters config change.
            set(
                CentralConfigurationConnectivity.fromApmServerConfig(
                    serviceName,
                    serviceDeployment,
                    apmServerConnectivityHolder.getConnectivityConfiguration()
                )
            )
        }
    }

    override fun onConfigChange() {
        configurationRegistry.reloadDynamicConfigurationOptions()
    }
}