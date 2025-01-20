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
package co.elastic.otel.android.features.diskbuffering.tools

import co.elastic.otel.android.common.internal.logging.Elog
import co.elastic.otel.android.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.services.appinfo.AppInfoService
import co.elastic.otel.android.internal.services.preferences.PreferencesService
import java.io.File
import java.io.IOException

class DiskManager internal constructor(
    private val appInfoService: AppInfoService,
    private val preferencesService: PreferencesService,
    private val diskBufferingConfiguration: DiskBufferingConfiguration
) {

    @Throws(IOException::class)
    fun getSignalsCacheDir(): File {
        val dir = File(appInfoService.getCacheDir(), "opentelemetry/signals")
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw IOException("Could not create dir $dir")
            }
        }
        return dir
    }

    @Throws(IOException::class)
    fun getTemporaryDir(): File {
        val dir = File(appInfoService.getCacheDir(), "opentelemetry/temp")
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw IOException("Could not create dir $dir")
            }
        }
        deleteFilesInside(dir)
        return dir
    }

    fun getMaxFolderSize(): Int {
        val storedSize = preferencesService.retrieveInt(MAX_FOLDER_SIZE_KEY, -1)
        if (storedSize != -1) {
            Elog.getLogger().debug("Returning max folder size from preferences: {}", storedSize)
            return storedSize
        }
        val requestedSize = diskBufferingConfiguration.maxCacheSize
        val availableCacheSize =
            appInfoService.getAvailableCacheSpace(requestedSize.toLong()).toInt()
        val calculatedSize =
            (availableCacheSize / 3) - diskBufferingConfiguration.maxCacheFileSize
        preferencesService.store(MAX_FOLDER_SIZE_KEY, calculatedSize)

        Elog.getLogger().debug(
            "Requested cache size: {}, available cache size: {}, folder size: {}",
            requestedSize,
            availableCacheSize,
            calculatedSize
        )
        return calculatedSize
    }

    fun getMaxCacheFileSize(): Int = diskBufferingConfiguration.maxCacheFileSize

    private fun deleteFilesInside(dir: File) {
        dir.listFiles()?.forEach {
            it.deleteRecursively()
        }
    }

    companion object {
        private const val MAX_FOLDER_SIZE_KEY = "max_signal_folder_size"

        fun create(
            serviceManager: ServiceManager,
            diskBufferingConfiguration: DiskBufferingConfiguration
        ): DiskManager {
            return DiskManager(
                serviceManager.getAppInfoService(),
                serviceManager.getPreferencesService(),
                diskBufferingConfiguration
            )
        }

    }
}
