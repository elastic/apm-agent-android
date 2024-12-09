package co.elastic.apm.android.sdk.features.centralconfig

import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import org.slf4j.Logger

class CentralConfigurationManager internal constructor(
    serviceManager: ServiceManager,
    private val centralConfiguration: CentralConfiguration
) {
    private val backgroundWorkService by lazy { serviceManager.getBackgroundWorkService() }
    private val logger: Logger = Elog.getLogger()

    companion object {
        private const val DEFAULT_POLLING_INTERVAL_IN_SECONDS = 60
    }

    internal fun initialize() {
        backgroundWorkService.submit {
            try {
                centralConfiguration.publishCachedConfig()
                val delayForNextPollInSeconds = centralConfiguration.sync()
                if (delayForNextPollInSeconds != null) {
                    scheduleInSeconds(delayForNextPollInSeconds)
                } else {
                    scheduleDefault()
                }
            } catch (t: Throwable) {
                Elog.getLogger().error("CentralConfigurationInitializer error", t)
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

    private inner class ConfigurationPoll : Runnable {
        override fun run() {
            try {
                val maxAgeInSeconds = centralConfiguration.sync()
                if (maxAgeInSeconds == null) {
                    logger.info("Central config returned max age is null")
                    scheduleDefault()
                } else {
                    scheduleInSeconds(maxAgeInSeconds)
                }
            } catch (t: Throwable) {
                logger.error("Central config poll error", t)
                scheduleDefault()
            }
        }
    }
}