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
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import java.io.IOException
import java.util.concurrent.TimeUnit
import org.slf4j.Logger

class CentralConfigurationManager private constructor(
    serviceManager: ServiceManager,
    private val centralConfiguration: CentralConfiguration,
    private val connectivityHolder: ConnectivityHolder
) {
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
        backgroundWorkService.scheduleOnce(
            TimeUnit.SECONDS.toMillis(seconds.toLong()),
            ConfigurationPoll()
        )
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

    companion object {
        private const val DEFAULT_POLLING_INTERVAL_IN_SECONDS = 60

        internal fun create(
            serviceManager: ServiceManager,
            serviceName: String,
            serviceDeployment: String?,
            connectivityHolder: ApmServerConnectivityManager.ConnectivityHolder
        ): CentralConfigurationManager {
            val centralConfigurationConnectivityHolder = ConnectivityHolder.fromApmServerConfig(
                serviceName, serviceDeployment, connectivityHolder
            )
            val centralConfiguration = CentralConfiguration.create(
                serviceManager,
                centralConfigurationConnectivityHolder
            )
            return CentralConfigurationManager(
                serviceManager,
                centralConfiguration,
                centralConfigurationConnectivityHolder
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
}