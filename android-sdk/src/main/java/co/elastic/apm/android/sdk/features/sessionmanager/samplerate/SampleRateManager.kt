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
package co.elastic.apm.android.sdk.features.sessionmanager.samplerate

import co.elastic.apm.android.sdk.features.centralconfig.CentralConfiguration
import co.elastic.apm.android.sdk.features.exportergate.latch.Latch
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.internal.services.kotlin.backgroundwork.BackgroundWorkService
import co.elastic.apm.android.sdk.internal.utilities.NumberTools
import co.elastic.apm.android.sdk.tools.cache.CacheHandler
import co.elastic.apm.android.sdk.tools.cache.PreferencesIntegerCacheHandler
import co.elastic.apm.android.sdk.tools.provider.Provider
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class SampleRateManager private constructor(
    private val sampleRateCentralConfigurationProvider: Provider<Double>,
    private val enabledExportingCache: CacheHandler<Int>,
    private val backgroundWorkService: BackgroundWorkService,
    private val numberTools: NumberTools,
    private val latch: WeakReference<Latch>
) {
    private val allowSignalExporting = AtomicBoolean(false)

    fun initialize() {
        backgroundWorkService.submit {
            setUpInitialPolicy()
            latch.get()?.open()
        }
    }

    private fun setUpInitialPolicy() {
        val enabledFlag = enabledExportingCache.retrieve()
        if (enabledFlag == FLAG_UNSET) {
            evaluateSampleRate()
        } else {
            allowSignalExporting.set(enabledFlag == FLAG_ENABLED)
        }
    }

    override fun onSessionChanged() {
        evaluateSampleRate()
    }

    fun allowSignalExporting(): Boolean {
        return allowSignalExporting.get()
    }

    private fun evaluateSampleRate() {
        val enable = shouldEnableSignalExporting()
        enabledExportingCache.store(if (enable) FLAG_ENABLED else FLAG_DISABLED)
        allowSignalExporting.set(enable)
    }

    private fun shouldEnableSignalExporting(): Boolean {
        val sampleRate = sampleRateCentralConfigurationProvider.get()
        if (sampleRate == 0.0) {
            return false
        } else if (sampleRate == 1.0) {
            return true
        }
        return numberTools.random() <= sampleRate
    }

    companion object {
        private const val KEY_ENABLE_SIGNAL_EXPORTING = "sample_rate_export_enabled"
        private const val FLAG_UNSET = -1
        private const val FLAG_ENABLED = 1
        private const val FLAG_DISABLED = 0

        internal fun create(
            serviceManager: ServiceManager,
            centralConfiguration: CentralConfiguration,
            gateLatch: Latch
        ): SampleRateManager {
            return SampleRateManager(
                { centralConfiguration.getSessionSampleRate() },
                PreferencesIntegerCacheHandler(
                    KEY_ENABLE_SIGNAL_EXPORTING,
                    serviceManager.getPreferencesService(),
                    FLAG_UNSET
                ),
                serviceManager.getBackgroundWorkService(),
                NumberTools.get(),
                WeakReference(gateLatch)
            )
        }
    }
}