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
package co.elastic.apm.android.sdk.integration

import co.elastic.apm.android.sdk.internal.api.ElasticOtelAgent
import co.elastic.apm.android.sdk.internal.services.re.ServiceManager
import co.elastic.apm.android.sdk.internal.services.re.appinfo.AppInfoService
import co.elastic.apm.android.sdk.internal.services.re.preferences.PreferencesService
import co.elastic.apm.android.sdk.testutils.ElasticAgentRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DiskBufferingTest {

    @get:Rule
    val agentRule = ElasticAgentRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Before
    fun setUp() {

    }

    @Test
    fun `Disk buffering enabled, happy path`() {
        val preferencesService = mockk<PreferencesService>()
        val appInfoService = mockk<AppInfoService>()
        val serviceManager = mockk<ServiceManager>()
        every { serviceManager.getPreferencesService() }.returns(preferencesService)
        every { serviceManager.getAppInfoService() }.returns(appInfoService)
        agentRule.initialize {
            val spy = spyk(it)
            every { spy.serviceManager }.returns(serviceManager)
            it
        }

        agentRule.sendSpan()
        agentRule.sendLog()
        agentRule.sendMetricCounter()

        agentRule.agent!!.close()

        // Nothing should have gotten exported because it was stored in disk.
        assertThat(agentRule.getFinishedSpans()).isEmpty()
        assertThat(agentRule.getFinishedLogRecords()).isEmpty()
        assertThat(agentRule.getFinishedMetrics()).isEmpty()

        // Re-init
        var config: ElasticOtelAgent.Configuration? = null
        agentRule.initialize {
            val spy = spyk(it)
            every { spy.serviceManager }.returns(serviceManager)
            config = it
            it
        }

        config!!.diskBufferingManager.exportFromDisk()

        // Now we should see the previously-stored signals exported.
        assertThat(agentRule.getFinishedSpans()).hasSize(1)
        assertThat(agentRule.getFinishedLogRecords()).hasSize(1)
        assertThat(agentRule.getFinishedMetrics()).hasSize(1)
    }
}