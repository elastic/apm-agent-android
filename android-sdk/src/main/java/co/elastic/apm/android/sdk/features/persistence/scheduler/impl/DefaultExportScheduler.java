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
import co.elastic.apm.android.sdk.features.persistence.SignalDiskExporter;
import co.elastic.apm.android.sdk.features.persistence.scheduler.ExportScheduler;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.periodicwork.ManagedPeriodicTask;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;

/**
 * Default export scheduler that executes periodically while the app is running.
 */
public final class DefaultExportScheduler extends ManagedPeriodicTask implements ExportScheduler {
    private final PeriodicWorkService periodicWorkService;
    private final long delayTimeInMillis;
    private final AtomicBoolean isDisabled = new AtomicBoolean(false);

    /**
     * Default export scheduler that executes periodically while the app is running.
     *
     * @param minDelayBetweenExportsInMillis - The minimum amount of time to wait until the next
     *                                       exporting iteration.
     */
    public DefaultExportScheduler(long minDelayBetweenExportsInMillis) {
        this(ServiceManager.get().getService(Service.Names.PERIODIC_WORK), minDelayBetweenExportsInMillis);
    }

    DefaultExportScheduler(PeriodicWorkService periodicWorkService, long delayTimeInMillis) {
        super();
        this.periodicWorkService = periodicWorkService;
        this.delayTimeInMillis = delayTimeInMillis;
    }

    @Override
    public void onPersistenceEnabled() {
        periodicWorkService.addTask(this);
        isDisabled.set(false);
    }

    @Override
    public void onPersistenceDisabled() {
        isDisabled.set(true);
    }

    @Override
    protected void onTaskRun() {
        if (isTaskFinished()) {
            return;
        }
        try {
            SignalDiskExporter signalDiskExporter = SignalDiskExporter.get();
            while (true) {
                if (!signalDiskExporter.exportBatchOfEach()) break;
            }
        } catch (IOException e) {
            Elog.getLogger().error("A problem happened while exporting signals from disk", e);
        }
    }

    @Override
    protected long getMinDelayBeforeNextRunInMillis() {
        return delayTimeInMillis;
    }

    @Override
    public boolean isTaskFinished() {
        return isDisabled.get();
    }
}
