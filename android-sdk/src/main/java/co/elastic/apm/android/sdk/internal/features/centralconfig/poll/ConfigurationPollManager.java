package co.elastic.apm.android.sdk.internal.features.centralconfig.poll;

import androidx.annotation.VisibleForTesting;

import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;

public final class ConfigurationPollManager implements Runnable {
    private static ConfigurationPollManager INSTANCE;
    private final ScheduledExecutorService executor;
    private final CentralConfigurationManager manager;
    private final Logger logger = Elog.getLogger();
    private final long defaultDelayInSeconds = TimeUnit.MINUTES.toSeconds(1);

    @VisibleForTesting
    public ConfigurationPollManager(CentralConfigurationManager manager, ScheduledExecutorService executor) {
        this.manager = manager;
        this.executor = executor;
    }

    public ConfigurationPollManager(CentralConfigurationManager manager) {
        this(manager, Executors.newSingleThreadScheduledExecutor(new PollThreadFactory()));
    }

    public static void initialize(CentralConfigurationManager manager, long initialDelayInSeconds) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already initialized");
        }
        INSTANCE = new ConfigurationPollManager(manager);
        INSTANCE.scheduleInSeconds(initialDelayInSeconds);
    }

    public static void resetForTest() {
        INSTANCE = null;
    }

    private void scheduleInSeconds(long delayInSeconds) {
        logger.info("Scheduling next central config poll");
        logger.debug("Next central config poll in {} seconds", delayInSeconds);
        executor.schedule(this, delayInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            Integer maxAgeInSeconds = manager.sync();
            if (maxAgeInSeconds == null) {
                logger.info("Central config returned max age is null");
                scheduleInSeconds(defaultDelayInSeconds);
            } else {
                scheduleInSeconds(maxAgeInSeconds);
            }
        } catch (Throwable t) {
            logger.error("Central config poll error", t);
            scheduleInSeconds(defaultDelayInSeconds);
        }
    }
}
