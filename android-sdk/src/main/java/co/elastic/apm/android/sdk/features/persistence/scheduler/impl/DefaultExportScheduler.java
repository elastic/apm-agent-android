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
package co.elastic.apm.android.sdk.features.persistence.scheduler.impl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.features.persistence.SignalFromDiskExporter;
import co.elastic.apm.android.sdk.features.persistence.scheduler.ExportScheduler;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicTask;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.Provider;

/**
 * Default export scheduler that executes periodically while the app is running.
 */
public final class DefaultExportScheduler implements PeriodicTask, ExportScheduler {
    private final Provider<PeriodicWorkService> periodicWorkService;
    private final Provider<PreferencesService> preferencesService;
    private final SystemTimeProvider timeProvider;
    private final long delayTimeInMillis;
    private final AtomicBoolean isDisabled = new AtomicBoolean(false);
    private static final String LAST_TIME_RUN_KEY = "last_time_exported_from_disk";
    private long lastTimeRunInMillis = 0;

    /**
     * Default export scheduler that executes periodically while the app is running.
     *
     * @param minDelayBetweenExportsInMillis - The minimum amount of time to wait until the next
     *                                       exporting iteration.
     */
    public DefaultExportScheduler(long minDelayBetweenExportsInMillis) {
        this(ServiceManager.getServiceProvider(Service.Names.PERIODIC_WORK),
                ServiceManager.getServiceProvider(Service.Names.PREFERENCES),
                SystemTimeProvider.get(),
                minDelayBetweenExportsInMillis);
    }

    DefaultExportScheduler(Provider<PeriodicWorkService> periodicWorkService,
                           Provider<PreferencesService> preferencesService,
                           SystemTimeProvider timeProvider,
                           long delayTimeInMillis) {
        super();
        this.periodicWorkService = periodicWorkService;
        this.preferencesService = preferencesService;
        this.delayTimeInMillis = delayTimeInMillis;
        this.timeProvider = timeProvider;
    }

    @Override
    public void onPersistenceEnabled() {
        Elog.getLogger().debug("On persistence enabled in default export scheduler");
        periodicWorkService.get().addTask(this);
        isDisabled.set(false);
        lastTimeRunInMillis = preferencesService.get().retrieveLong(LAST_TIME_RUN_KEY, 0);
    }

    @Override
    public void onPersistenceDisabled() {
        Elog.getLogger().debug("On persistence disabled in default export scheduler");
        isDisabled.set(true);
    }

    @Override
    public boolean shouldRunTask() {
        return !isDisabled.get() && delayHasPassed();
    }

    private boolean delayHasPassed() {
        return timeProvider.getCurrentTimeMillis() >= (lastTimeRunInMillis + delayTimeInMillis);
    }

    @Override
    public void runTask() {
        Elog.getLogger().debug("Running default export scheduler");
        lastTimeRunInMillis = timeProvider.getCurrentTimeMillis();
        preferencesService.get().store(LAST_TIME_RUN_KEY, lastTimeRunInMillis);
        try {
            SignalFromDiskExporter signalFromDiskExporter = SignalFromDiskExporter.get();
            while (true) {
                if (!signalFromDiskExporter.exportBatchOfEach()) break;
            }
        } catch (IOException e) {
            Elog.getLogger().error("A problem happened while exporting signals from disk", e);
        }
        Elog.getLogger().debug("Finished running default export scheduler");
    }

    @Override
    public boolean isTaskFinished() {
        return isDisabled.get();
    }
}
