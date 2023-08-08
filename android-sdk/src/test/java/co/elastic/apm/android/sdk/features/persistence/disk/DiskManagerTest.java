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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import co.elastic.apm.android.sdk.internal.configuration.impl.SignalPersistenceConfiguration;
import co.elastic.apm.android.sdk.internal.services.appinfo.AppInfoService;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;

public class DiskManagerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private AppInfoService appInfoService;
    private PreferencesService preferencesService;
    private SignalPersistenceConfiguration persistenceConfiguration;
    private DiskManager diskManager;
    private File cacheDir;
    private static final String MAX_FOLDER_SIZE_KEY = "max_signal_folder_size";

    @Before
    public void setUp() throws IOException {
        cacheDir = temporaryFolder.newFolder("app_cache");
        appInfoService = mock(AppInfoService.class);
        preferencesService = mock(PreferencesService.class);
        persistenceConfiguration = mock(SignalPersistenceConfiguration.class);
        doReturn(cacheDir).when(appInfoService).getCacheDir();
        diskManager = new DiskManager(appInfoService, preferencesService, persistenceConfiguration);
    }

    @Test
    public void provideSignalCacheDir() {
        assertEquals(new File(cacheDir, "opentelemetry/signals"), diskManager.getSignalsCacheDir());
    }

    @Test
    public void provideTemporaryDir() {
        assertEquals(new File(cacheDir, "opentelemetry/temp"), diskManager.getTemporaryDir());
    }

    @Test
    public void calculateMaxSignalFolderSize() {
        long maxCacheSize = 10 * 1024 * 1024; // 10 MB
        int maxCacheFileSize = 1024 * 1024; // 1 MB
        doReturn((int) maxCacheSize).when(persistenceConfiguration).getMaxCacheSize();
        doReturn(maxCacheFileSize).when(persistenceConfiguration).getMaxCacheFileSize();
        doReturn(maxCacheSize).when(appInfoService).getAvailableCacheSpace(maxCacheSize);
        doReturn(-1).when(preferencesService).retrieveInt(MAX_FOLDER_SIZE_KEY, -1);

        // Expects the size of a single signal type folder minus the size of a cache file, to use as temporary space for reading.
        int expected = 2_446_677;
        assertEquals(expected, diskManager.getMaxFolderSize());
        verify(preferencesService).store(MAX_FOLDER_SIZE_KEY, expected);

        // On a second call, should get the value from the preferences.
        clearInvocations(appInfoService, persistenceConfiguration, preferencesService);
        doReturn(expected).when(preferencesService).retrieveInt(MAX_FOLDER_SIZE_KEY, -1);
        assertEquals(expected, diskManager.getMaxFolderSize());
        verify(preferencesService).retrieveInt(MAX_FOLDER_SIZE_KEY, -1);
        verifyNoMoreInteractions(preferencesService);
        verifyNoInteractions(appInfoService, persistenceConfiguration);
    }
}