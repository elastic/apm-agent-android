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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.utilities.providers.LazyProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.Provider;

public final class ConfigurationPollManager implements Runnable {
    private static ConfigurationPollManager INSTANCE;
    private final Provider<ScheduledExecutorService> executorProvider;
    private final CentralConfigurationManager manager;
    private final Logger logger = Elog.getLogger();
    private static final long DEFAULT_DELAY_IN_SECONDS = 60;

    @VisibleForTesting
    public ConfigurationPollManager(CentralConfigurationManager manager, Provider<ScheduledExecutorService> executorProvider) {
        this.manager = manager;
        this.executorProvider = executorProvider;
    }

    public ConfigurationPollManager(CentralConfigurationManager manager) {
        this(manager, LazyProvider.of(() -> Executors.newSingleThreadScheduledExecutor(new PollThreadFactory())));
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

    public void scheduleInSeconds(long delayInSeconds) {
        logger.info("Scheduling next central config poll");
        logger.debug("Next central config poll in {} seconds", delayInSeconds);
        executorProvider.get().schedule(this, delayInSeconds, TimeUnit.SECONDS);
    }

    public void scheduleDefault() {
        scheduleInSeconds(DEFAULT_DELAY_IN_SECONDS);
    }

    @Override
    public void run() {
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
}
