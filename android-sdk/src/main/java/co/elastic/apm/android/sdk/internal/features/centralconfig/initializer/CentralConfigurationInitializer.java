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

import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.singletask.BackgroundExecutor;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.singletask.Result;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.singletask.impl.SimpleBackgroundExecutor;

public final class CentralConfigurationInitializer implements BackgroundExecutor.Callback<Integer> {
    private final BackgroundExecutor executor;
    private final CentralConfigurationManager manager;
    private final ConfigurationPollManager pollManager;

    @VisibleForTesting
    public CentralConfigurationInitializer(BackgroundExecutor executor, CentralConfigurationManager manager, ConfigurationPollManager pollManager) {
        this.executor = executor;
        this.manager = manager;
        this.pollManager = pollManager;
    }

    public CentralConfigurationInitializer(CentralConfigurationManager manager, ConfigurationPollManager pollManager) {
        this(new SimpleBackgroundExecutor(), manager, pollManager);
    }

    public CentralConfigurationManager getManager() {
        return manager;
    }

    public ConfigurationPollManager getPollManager() {
        return pollManager;
    }

    public void initialize() {
        executor.execute(() -> {
            manager.publishCachedConfig();
            return manager.sync();
        }, this);
    }

    @Override
    public void onFinish(Result<Integer> result) {
        if (result.isSuccess && result.value != null) {
            pollManager.scheduleInSeconds(result.value);
        } else {
            pollManager.scheduleDefault();
        }
    }
}
