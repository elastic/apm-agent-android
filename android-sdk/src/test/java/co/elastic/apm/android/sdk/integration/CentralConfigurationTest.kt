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

import co.elastic.apm.android.sdk.internal.configuration.Configurations
import co.elastic.apm.android.sdk.internal.configuration.impl.AllInstrumentationConfiguration
import co.elastic.apm.android.sdk.testutils.ElasticAgentRule
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CentralConfigurationTest {
    @get:Rule
    val agentRule = ElasticAgentRule()

    @Test
    fun `When recording is enabled, export all signals`() {
        initializeAgent()

        agentRule.sendSpan()
        agentRule.sendLog()
        agentRule.sendMetricCounter()

        assertThat(agentRule.getFinishedSpans()).hasSize(1)
        assertThat(agentRule.getFinishedLogRecords()).hasSize(1)
        assertThat(agentRule.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `When recording is not enables, do not export any signal`() {
        initializeAgent()
        every { Configurations.get(AllInstrumentationConfiguration::class.java).isEnabled }.returns(
            false
        )

        agentRule.sendSpan()
        agentRule.sendLog()
        agentRule.sendMetricCounter()

        assertThat(agentRule.getFinishedSpans()).hasSize(0)
        assertThat(agentRule.getFinishedLogRecords()).hasSize(0)
        assertThat(agentRule.getFinishedMetrics()).hasSize(0)
    }

    @Test
    fun `When sample rate is zero, do no export any signal`() {
        initializeAgent(sampleRate = 0.0)

        agentRule.sendSpan()
        agentRule.sendLog()
        agentRule.sendMetricCounter()

        assertThat(agentRule.getFinishedSpans()).hasSize(0)
        assertThat(agentRule.getFinishedLogRecords()).hasSize(0)
        assertThat(agentRule.getFinishedMetrics()).hasSize(0)
    }

    private fun initializeAgent(sampleRate: Double = 1.0) {
        agentRule.initialize(configurationInterceptor = {
            it.setSampleRate(sampleRate)
        })
    }
}