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
package co.elastic.apm.android.sdk.internal.features.centralconfig.initializer;

import androidx.annotation.VisibleForTesting;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicTask;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;

public final class CentralConfigurationInitializer extends PeriodicTask {
    private final CentralConfigurationManager manager;
    private final ConfigurationPollManager pollManager;
    private final PeriodicWorkService periodicWorkService;

    @VisibleForTesting
    public CentralConfigurationInitializer(CentralConfigurationManager manager,
                                           ConfigurationPollManager pollManager,
                                           PeriodicWorkService periodicWorkService) {
        super();
        this.manager = manager;
        this.pollManager = pollManager;
        this.periodicWorkService = periodicWorkService;
    }

    public CentralConfigurationInitializer(CentralConfigurationManager manager, ConfigurationPollManager pollManager) {
        this(manager, pollManager, ServiceManager.get().getService(Service.Names.PERIODIC_WORK));
    }

    public CentralConfigurationManager getManager() {
        return manager;
    }

    public ConfigurationPollManager getPollManager() {
        return pollManager;
    }

    @Override
    protected void onPeriodicTaskRun() {
        try {
            manager.publishCachedConfig();
            Integer delayForNextPollInSeconds = manager.sync();
            if (delayForNextPollInSeconds != null) {
                pollManager.scheduleInSeconds(delayForNextPollInSeconds);
            } else {
                pollManager.scheduleDefault();
            }
        } catch (Throwable t) {
            Elog.getLogger().error("CentralConfigurationInitializer error", t);
            pollManager.scheduleDefault();
        }
        periodicWorkService.addTask(pollManager);
    }

    @Override
    protected long getMinDelayBeforeNextRunInMillis() {
        // Doesn't need to delay its execution further than the Periodic work service internal delays.
        return 0;
    }

    @Override
    public boolean isFinished() {
        // Will only run once.
        return true;
    }
}
