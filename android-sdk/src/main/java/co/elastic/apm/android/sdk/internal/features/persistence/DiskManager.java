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
package co.elastic.apm.android.sdk.internal.features.persistence;

import java.io.File;
import java.io.IOException;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.SignalPersistenceConfiguration;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.appinfo.AppInfoService;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;

public final class DiskManager {
    private final AppInfoService appInfoService;
    private final PreferencesService preferencesService;
    private final SignalPersistenceConfiguration persistenceConfiguration;
    private static final String MAX_FOLDER_SIZE_KEY = "max_signal_folder_size";

    public static DiskManager create() {
        ServiceManager serviceManager = ServiceManager.get();
        return new DiskManager(serviceManager.getService(Service.Names.APP_INFO),
                serviceManager.getService(Service.Names.PREFERENCES),
                Configurations.get(SignalPersistenceConfiguration.class));
    }

    DiskManager(AppInfoService appInfoService, PreferencesService preferencesService, SignalPersistenceConfiguration persistenceConfiguration) {
        this.appInfoService = appInfoService;
        this.preferencesService = preferencesService;
        this.persistenceConfiguration = persistenceConfiguration;
    }

    public File getSignalsCacheDir() throws IOException {
        File dir = new File(appInfoService.getCacheDir(), "opentelemetry/signals");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create dir " + dir);
            }
        }
        return dir;
    }

    public File getTemporaryDir() throws IOException {
        File dir = new File(appInfoService.getCacheDir(), "opentelemetry/temp");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create dir " + dir);
            }
        }
        deleteFiles(dir);
        return dir;
    }

    public int getMaxFolderSize() {
        int storedSize = preferencesService.retrieveInt(MAX_FOLDER_SIZE_KEY, -1);
        if (storedSize != -1) {
            Elog.getLogger().debug("Returning max folder size from preferences: {}", storedSize);
            return storedSize;
        }
        int requestedSize = persistenceConfiguration.getMaxCacheSize();
        int availableCacheSize = (int) appInfoService.getAvailableCacheSpace(requestedSize);
        int calculatedSize = (availableCacheSize / 3) - persistenceConfiguration.getMaxCacheFileSize();
        preferencesService.store(MAX_FOLDER_SIZE_KEY, calculatedSize);

        Elog.getLogger().debug("Requested cache size: {}, available cache size: {}, folder size: {}", requestedSize, availableCacheSize, calculatedSize);
        return calculatedSize;
    }

    public int getMaxCacheFileSize() {
        return persistenceConfiguration.getMaxCacheFileSize();
    }

    private static void deleteFiles(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFiles(file);
                }
                file.delete();
            }
        }
    }
}
