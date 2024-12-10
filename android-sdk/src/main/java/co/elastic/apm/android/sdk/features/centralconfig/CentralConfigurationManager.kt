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
import co.elastic.apm.android.sdk.connectivity.ConnectivityConfigurationManager
import co.elastic.apm.android.sdk.features.apmserver.ApmServerConnectivityManager
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import java.io.IOException
import org.slf4j.Logger

class CentralConfigurationManager private constructor(
    serviceManager: ServiceManager,
    private val centralConfiguration: CentralConfiguration,
    private val configurationManager: ConfigurationManager
) {
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }
    private val logger: Logger = Elog.getLogger()

    companion object {
        private const val DEFAULT_POLLING_INTERVAL_IN_SECONDS = 60

        internal fun create(
            serviceManager: ServiceManager,
            serviceName: String,
            serviceDeployment: String?,
            apmServerConfigurationManager: ApmServerConnectivityManager.ConfigurationManager
        ): CentralConfigurationManager {
            val centralConfigurationConfigurationManager = ConfigurationManager.fromApmServerConfig(
                serviceName, serviceDeployment, apmServerConfigurationManager
            )
            val centralConfiguration = CentralConfiguration.create(
                serviceManager,
                centralConfigurationConfigurationManager
            )
            return CentralConfigurationManager(
                serviceManager,
                centralConfiguration,
                centralConfigurationConfigurationManager
            )
        }
    }

    fun getConnectivityConfiguration(): CentralConfigurationConnectivity {
        return configurationManager.get() as CentralConfigurationConnectivity
    }

    fun setConnectivityConfiguration(configuration: CentralConfigurationConnectivity) {
        configurationManager.unlinkFromExportersConfig()
        configurationManager.set(configuration)
    }

    internal fun initialize() {
        backgroundWorkService.submit {
            try {
                centralConfiguration.publishCachedConfig()
                doPoll()
            } catch (t: Throwable) {
                logger.error("CentralConfiguration initialization error", t)
                scheduleDefault()
            }
        }
    }

    private fun scheduleDefault() {
        scheduleInSeconds(DEFAULT_POLLING_INTERVAL_IN_SECONDS)
    }

    private fun scheduleInSeconds(seconds: Int) {
        backgroundWorkService.schedule(ConfigurationPoll(), seconds)
    }

    @WorkerThread
    @Throws(IOException::class)
    private fun doPoll() {
        val delayForNextPollInSeconds = centralConfiguration.sync()
        if (delayForNextPollInSeconds != null) {
            logger.info("Central config returned max age is null")
            scheduleInSeconds(delayForNextPollInSeconds)
        } else {
            scheduleDefault()
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

    internal class ConfigurationManager(
        connectivity: CentralConfigurationConnectivity,
        private val apmServerConfigurationManager: ApmServerConnectivityManager.ConfigurationManager,
        private val serviceName: String,
        private val serviceDeployment: String?
    ) : ConnectivityConfigurationManager(connectivity), ConnectivityConfigurationManager.Listener {

        companion object {
            fun fromApmServerConfig(
                serviceName: String,
                serviceDeployment: String?,
                apmServerConfigurationManager: ApmServerConnectivityManager.ConfigurationManager
            ): ConfigurationManager {
                val instance = ConfigurationManager(
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
            apmServerConfigurationManager.removeListener(this)
        }

        override fun onConnectivityConfigurationChange() {
            // On apm server exporters config change.
            set(
                CentralConfigurationConnectivity.fromApmServerConfig(
                    serviceName,
                    serviceDeployment,
                    apmServerConfigurationManager.getConnectivityConfiguration()
                )
            )
        }
    }
}