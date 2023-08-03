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
package co.elastic.apm.android.sdk.internal.services.appinfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.storage.StorageManager;

import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.services.Service;

public class AppInfoService implements Service {
    private final Context appContext;

    public AppInfoService(Context appContext) {
        this.appContext = appContext;
    }

    public boolean isPermissionGranted(String permissionName) {
        return appContext.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isInDebugMode() {
        return (appContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public int getVersionCode() {
        try {
            PackageInfo packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Elog.getLogger().error("Error providing versionCode", e);
            return 0;
        }
    }

    public File getCacheDir() {
        return appContext.getCacheDir();
    }

    @WorkerThread
    public long getAvailableCacheSpace(long maxSpaceNeeded) {
        File cacheDir = getCacheDir();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return getLegacyAvailableSpace(cacheDir, maxSpaceNeeded);
        }
        return getAvailableSpace(cacheDir, maxSpaceNeeded);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private long getAvailableSpace(File directory, long maxSpaceNeeded) {
        Elog.getLogger().debug("Getting available space for {}, max needed is: {}", directory, maxSpaceNeeded);
        try {
            StorageManager storageManager = appContext.getSystemService(StorageManager.class);
            UUID appSpecificInternalDirUuid = storageManager.getUuidForPath(directory);
            // Get the minimum amount of allocatable space.
            long spaceToAllocate = Math.min(storageManager.getAllocatableBytes(appSpecificInternalDirUuid), maxSpaceNeeded);
            // Ensure the space is available by asking the OS to clear stale cache if needed.
            storageManager.allocateBytes(appSpecificInternalDirUuid, spaceToAllocate);
            return spaceToAllocate;
        } catch (IOException e) {
            Elog.getLogger().error("Failed to get available space", e);
            return getLegacyAvailableSpace(directory, maxSpaceNeeded);
        }
    }

    private long getLegacyAvailableSpace(File directory, long maxSpaceNeeded) {
        Elog.getLogger().debug("Getting legacy available space for {}, max needed is: {}", directory, maxSpaceNeeded);
        return Math.min(directory.getUsableSpace(), maxSpaceNeeded);
    }

    @Override
    public void start() {
        // No operation.
    }

    @Override
    public void stop() {
        // No operation.
    }

    @Override
    public String name() {
        return Service.Names.APP_INFO;
    }
}
