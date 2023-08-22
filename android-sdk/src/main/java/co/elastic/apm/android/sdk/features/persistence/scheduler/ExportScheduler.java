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
package co.elastic.apm.android.sdk.features.persistence.scheduler;

import co.elastic.apm.android.sdk.features.persistence.scheduler.impl.DefaultExportScheduler;

/**
 * Takes care of setting up the process to read and export previously stored signals.
 */
public interface ExportScheduler {

    /**
     * Provides an export scheduler that executes periodically while the app is running.
     *
     * @param minDelayBetweenExportsInMillis - The minimum amount of time in milliseconds to wait until the next
     *                                       exporting iteration.
     */
    static ExportScheduler getDefault(long minDelayBetweenExportsInMillis) {
        return new DefaultExportScheduler(minDelayBetweenExportsInMillis);
    }

    /**
     * This is called during the agent initialization when the persistence feature is enabled. It
     * should be the place where any setup/initialization of the reading/exporting scheduling is done.
     */
    void onPersistenceEnabled();

    /**
     * This is called during the agent initialization when the persistence feature is disabled.
     * It might be used to cancel previously scheduled periodic work when the feature was enabled.
     */
    void onPersistenceDisabled();
}
