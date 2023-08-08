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
package co.elastic.apm.android.sdk.features.persistence.disk;

import java.io.File;

import co.elastic.apm.android.sdk.internal.configuration.impl.SignalPersistenceConfiguration;
import co.elastic.apm.android.sdk.internal.services.appinfo.AppInfoService;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;

public final class DiskManager {
    private final AppInfoService appInfoService;
    private final PreferencesService preferencesService;
    private final SignalPersistenceConfiguration persistenceConfiguration;
    private static final String MAX_FOLDER_SIZE_KEY = "max_signal_folder_size";

    public DiskManager(AppInfoService appInfoService, PreferencesService preferencesService, SignalPersistenceConfiguration persistenceConfiguration) {
        this.appInfoService = appInfoService;
        this.preferencesService = preferencesService;
        this.persistenceConfiguration = persistenceConfiguration;
    }

    public File getSignalsCacheDir() {
        return new File(appInfoService.getCacheDir(), "opentelemetry/signals");
    }

    public File getTemporaryDir() {
        return new File(appInfoService.getCacheDir(), "opentelemetry/temp");
    }

    public int getMaxFolderSize() {
        int storedSize = preferencesService.retrieveInt(MAX_FOLDER_SIZE_KEY, -1);
        if (storedSize != -1) {
            return storedSize;
        }
        int calculatedSize = ((persistenceConfiguration.getMaxCacheSize()) / 3) - persistenceConfiguration.getMaxCacheFileSize();
        preferencesService.store(MAX_FOLDER_SIZE_KEY, calculatedSize);
        return calculatedSize;
    }
}
