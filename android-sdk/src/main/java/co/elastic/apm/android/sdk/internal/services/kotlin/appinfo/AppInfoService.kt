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

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
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

class AppInfoService(private val context: Context) : Service {

    fun isPermissionGranted(permissionName: String): Boolean {
        return context.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED
    }

    fun isInDebugMode(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun getVersionCode(): Int {
        return getPackageInfo()?.versionCode ?: 0
    }

    fun getVersionName(): String? {
        return getPackageInfo()?.versionName
    }

    fun getCacheDir(): File = context.cacheDir

    fun getFilesDir(): File = context.filesDir

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
            val storageManager = context.getSystemService(
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

    private fun getPackageInfo(): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Elog.getLogger().error("Package info not found", e)
            null
        }
    }
}