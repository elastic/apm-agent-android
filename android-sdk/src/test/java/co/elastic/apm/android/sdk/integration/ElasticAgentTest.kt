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

import co.elastic.apm.android.sdk.ElasticAgent
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ElasticAgentTest {
    private lateinit var webServer: MockWebServer
    private lateinit var agent: ElasticAgent

    @Before
    fun setUp() {
        webServer = MockWebServer()
        webServer.start()
    }

    @After
    fun tearDown() {
        webServer.close()
    }

    @Test
    fun `Validate initial apm server params`() {
        agent = ElasticAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl(webServer.url("/").toString())
            .setServiceName("my-app")
            .build()

        webServer.enqueue(MockResponse())

        sendSpan()
        agent.close()

        val request = webServer.takeRequest(1, TimeUnit.SECONDS)!!

        assertThat(request.path).isEqualTo("v1/traces")
    }

    private fun sendSpan() {
        agent.getOpenTelemetry().getTracer("TestTracer")
            .spanBuilder("span-name")
            .startSpan()
            .end()
    }
}