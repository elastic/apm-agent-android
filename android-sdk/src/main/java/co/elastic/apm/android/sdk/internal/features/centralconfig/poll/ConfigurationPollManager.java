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
package co.elastic.apm.android.sdk.internal.features.centralconfig.poll;

import androidx.annotation.VisibleForTesting;

import org.slf4j.Logger;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicTask;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;

public final class ConfigurationPollManager extends PeriodicTask {
    private static ConfigurationPollManager INSTANCE;
    private static final long DEFAULT_DELAY_IN_SECONDS = 60;
    private final CentralConfigurationManager manager;
    private final Logger logger = Elog.getLogger();
    private long delayForNextRunInMillis = DEFAULT_DELAY_IN_SECONDS * 1000;

    public static ConfigurationPollManager create(CentralConfigurationManager manager) {
        return create(manager, ServiceManager.get().getService(Service.Names.PERIODIC_WORK));
    }

    @VisibleForTesting
    public static ConfigurationPollManager create(CentralConfigurationManager manager, PeriodicWorkService periodicWorkService) {
        ConfigurationPollManager configurationPollManager = new ConfigurationPollManager(manager);
        periodicWorkService.addTask(configurationPollManager);
        return configurationPollManager;
    }

    @VisibleForTesting
    private ConfigurationPollManager(CentralConfigurationManager manager) {
        super();
        this.manager = manager;
    }

    public static ConfigurationPollManager get() {
        return INSTANCE;
    }

    public static void set(ConfigurationPollManager pollManager) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already initialized");
        }
        INSTANCE = pollManager;
    }

    public static void resetForTest() {
        INSTANCE = null;
    }

    public synchronized void scheduleInSeconds(long delayInSeconds) {
        logger.info("Scheduling next central config poll");
        logger.debug("Next central config poll in {} seconds", delayInSeconds);
        delayForNextRunInMillis = delayInSeconds * 1000;
    }

    public void scheduleDefault() {
        scheduleInSeconds(DEFAULT_DELAY_IN_SECONDS);
    }

    @Override
    protected void onPeriodicTaskRun() {
        try {
            Integer maxAgeInSeconds = manager.sync();
            if (maxAgeInSeconds == null) {
                logger.info("Central config returned max age is null");
                scheduleDefault();
            } else {
                scheduleInSeconds(maxAgeInSeconds);
            }
        } catch (Throwable t) {
            logger.error("Central config poll error", t);
            scheduleDefault();
        }
    }

    @Override
    protected long getMillisToWaitBeforeNextRun() {
        return delayForNextRunInMillis;
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
