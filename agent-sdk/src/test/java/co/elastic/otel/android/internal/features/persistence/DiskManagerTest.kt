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
package co.elastic.otel.android.internal.features.persistence

import co.elastic.otel.android.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.features.diskbuffering.tools.DiskManager
import co.elastic.otel.android.internal.services.appinfo.AppInfoService
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.util.Objects
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DiskManagerTest {
    @get:Rule
    var temporaryFolder: TemporaryFolder = TemporaryFolder()
    private lateinit var appInfoService: AppInfoService
    private lateinit var cacheDir: File

    @Before
    fun setUp() {
        cacheDir = temporaryFolder.newFolder("app_cache")
        appInfoService = mockk()
        every { appInfoService.getCacheDir() }.returns(cacheDir)
    }

    @Test
    fun provideSignalCacheDir() {
        val expected = File(cacheDir, "opentelemetry/signals")
        assertEquals(expected, createInstance().getSignalsCacheDir())
        assertTrue(expected.exists())
    }

    @Test
    fun provideTemporaryDir() {
        val expected = File(cacheDir, "opentelemetry/temp")
        assertEquals(expected, createInstance().getTemporaryDir())
        assertTrue(expected.exists())
    }

    @Test
    fun cleanupTemporaryDirBeforeProvidingIt() {
        val dir = File(cacheDir, "opentelemetry/temp")
        assertTrue(dir.mkdirs())
        assertTrue(File(dir, "somefile.tmp").createNewFile())
        assertTrue(File(dir, "some_other_file.tmp").createNewFile())
        assertTrue(File(dir, "somedir").mkdirs())
        assertTrue(File(dir, "somedir/some_other_file.tmp").createNewFile())

        val temporaryDir = createInstance().getTemporaryDir()

        assertTrue(temporaryDir.exists())
        assertEquals(0, Objects.requireNonNull(temporaryDir.listFiles()).size.toLong())
    }

    @Test
    fun getMaxCacheFileSize() {
        val persistenceSize = 1024 * 1024 * 2

        val diskManager =
            createInstance(DiskBufferingConfiguration.Enabled(maxCacheFileSize = persistenceSize))

        assertThat(persistenceSize.toLong()).isEqualTo(diskManager.getMaxCacheFileSize().toLong())
    }

    @Test
    fun getMaxSignalFolderSize() {
        val maxCacheSize = 15 * 1024 * 1024 // 15 MB

        val diskManager =
            createInstance(DiskBufferingConfiguration.Enabled(maxCacheSize = maxCacheSize))

        // Expects the size of a single signal type folder.
        val expected = 5242880L
        assertThat(expected).isEqualTo(diskManager.getMaxFolderSize().toLong())
    }

    private fun createInstance(configuration: DiskBufferingConfiguration.Enabled = DiskBufferingConfiguration.enabled() as DiskBufferingConfiguration.Enabled): DiskManager {
        return DiskManager(appInfoService, configuration)
    }

    companion object {
        private const val MAX_FOLDER_SIZE_KEY = "max_signal_folder_size"
    }
}