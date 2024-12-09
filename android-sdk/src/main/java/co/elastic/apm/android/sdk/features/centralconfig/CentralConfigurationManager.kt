package co.elastic.apm.android.sdk.features.centralconfig

import androidx.annotation.WorkerThread
import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import java.io.IOException
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
}