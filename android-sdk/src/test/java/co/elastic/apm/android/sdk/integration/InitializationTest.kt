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

import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager
import co.elastic.apm.android.sdk.internal.services.Service
import co.elastic.apm.android.sdk.internal.services.ServiceManager
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService
import co.elastic.apm.android.sdk.testutils.ElasticApmAgentRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InitializationTest {

    @get:Rule
    val agentRule = ElasticApmAgentRule()

    @Test
    fun `Central config initialization`() {
        val pollManager = mockk<ConfigurationPollManager>()
        agentRule.setCentralConfigurationInitializerInterceptor {
            every { it.pollManager }.returns(pollManager)
        }
        agentRule.initialize()

        verify { periodicWorkService.addTask(agentRule.centralConfigurationInitializer) }
        assertThat(ConfigurationPollManager.get()).isEqualTo(pollManager)
    }

    companion object {
        private val periodicWorkService: PeriodicWorkService
            get() = ServiceManager.get().getService(Service.Names.PERIODIC_WORK)
    }
}
