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
package co.elastic.apm.android.sdk.internal.services.kotlin.appinfo

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.internal.services.kotlin.Service
import java.io.File
import java.io.IOException
import kotlin.math.min

class AppInfoService(private val application: Application) : Service {

    fun isPermissionGranted(permissionName: String): Boolean {
        return application.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED
    }

    fun isInDebugMode(): Boolean {
        return (application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun getVersionCode(): Int {
        try {
            val packageInfo =
                application.packageManager.getPackageInfo(application.packageName, 0)
            return packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Elog.getLogger().error("Error providing versionCode", e)
            return 0
        }
    }

    fun getCacheDir(): File = application.cacheDir

    fun getFilesDir(): File = application.filesDir

    @WorkerThread
    fun getAvailableCacheSpace(maxSpaceNeeded: Long): Long {
        val cacheDir = getCacheDir()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return getLegacyAvailableSpace(cacheDir, maxSpaceNeeded)
        }
        return getAvailableSpace(cacheDir, maxSpaceNeeded)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getAvailableSpace(directory: File, maxSpaceNeeded: Long): Long {
        Elog.getLogger()
            .debug("Getting available space for {}, max needed is: {}", directory, maxSpaceNeeded)
        try {
            val storageManager = application.getSystemService(
                StorageManager::class.java
            )
            val appSpecificInternalDirUuid = storageManager.getUuidForPath(directory)
            // Get the minimum amount of allocatable space.
            val spaceToAllocate = min(
                storageManager.getAllocatableBytes(appSpecificInternalDirUuid).toDouble(),
                maxSpaceNeeded.toDouble()
            ).toLong()
            // Ensure the space is available by asking the OS to clear stale cache if needed.
            storageManager.allocateBytes(appSpecificInternalDirUuid, spaceToAllocate)
            return spaceToAllocate
        } catch (e: IOException) {
            Elog.getLogger().error("Failed to get available space", e)
            return getLegacyAvailableSpace(directory, maxSpaceNeeded)
        }
    }

    private fun getLegacyAvailableSpace(directory: File, maxSpaceNeeded: Long): Long {
        Elog.getLogger().debug(
            "Getting legacy available space for {}, max needed is: {}",
            directory,
            maxSpaceNeeded
        )
        return min(directory.usableSpace.toDouble(), maxSpaceNeeded.toDouble()).toLong()
    }
}