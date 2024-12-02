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

import android.app.Activity
import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration
import co.elastic.apm.android.sdk.internal.features.launchtime.LaunchTimeTracker
import co.elastic.apm.android.sdk.testutils.ElasticApmAgentRule
import org.assertj.core.api.AbstractStringAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppLaunchTimeTest {

    @get:Rule
    val agentRule = ElasticApmAgentRule()

    @After
    fun tearDown() {
        LaunchTimeTracker.resetForTest()
    }

    @Test
    fun `Track startup time when the first activity is launched`() {
        agentRule.initialize()
        Robolectric.buildActivity(MyMainActivity::class.java).use { controller ->
            controller.create()
            // Checking that there's no metrics up to this point
            assertThat(agentRule.getFinishedMetrics()).hasSize(0)

            controller.start().postCreate(null)

            validateMetricSent()
        }
    }

    @Test
    fun `Send metric only once, regardless of how many times metrics are flushed`() {
        agentRule.initialize()
        Robolectric.buildActivity(MyMainActivity::class.java).use { controller ->
            controller.setup()
            agentRule.flushMetrics()
            agentRule.flushMetrics()

            validateMetricSent()
        }
    }

    @Test
    fun `Track the time only for the first activity`() {
        agentRule.initialize()
        Robolectric.buildActivity(MyMainActivity::class.java).use { controller ->
            controller.setup()
            validateMetricSent()
        }
        Robolectric.buildActivity(AnotherActivity::class.java)
            .use { controller ->
                controller.setup()
                assertThat(agentRule.getFinishedMetrics()).hasSize(0)
            }
    }

    @Test
    fun `Do not track time when the instrumentation is disabled`() {
        agentRule.initialize(configurationInterceptor = {
            it.setInstrumentationConfiguration(
                InstrumentationConfiguration.builder().enableAppLaunchTime(false).build()
            )
        })

        Robolectric.buildActivity(MyMainActivity::class.java).use { controller ->
            controller.setup()
            assertThat(agentRule.getFinishedMetrics()).hasSize(0)
        }
    }

    private fun validateMetricSent(): AbstractStringAssert<*>? {
        val finishedMetrics = agentRule.getFinishedMetrics()
        assertThat(finishedMetrics).hasSize(1)
        return assertThat(finishedMetrics.first().name).isEqualTo("application.launch.time")
    }

    private class MyMainActivity : Activity()
    private class AnotherActivity : Activity()
}
