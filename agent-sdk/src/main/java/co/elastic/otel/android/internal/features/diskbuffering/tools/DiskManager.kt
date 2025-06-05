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
package co.elastic.otel.android.internal.features.diskbuffering.tools

import co.elastic.otel.android.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.services.appinfo.AppInfoService
import java.io.File
import java.io.IOException

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class DiskManager internal constructor(
    private val appInfoService: AppInfoService,
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
        return diskBufferingConfiguration.maxCacheSize / 3
    }

    fun getMaxCacheFileSize(): Int = diskBufferingConfiguration.maxCacheFileSize

    private fun deleteFilesInside(dir: File) {
        dir.listFiles()?.forEach {
            it.deleteRecursively()
        }
    }

    companion object {
        fun create(
            serviceManager: ServiceManager,
            diskBufferingConfiguration: DiskBufferingConfiguration
        ): DiskManager {
            return DiskManager(
                serviceManager.getAppInfoService(),
                diskBufferingConfiguration
            )
        }

    }
}
