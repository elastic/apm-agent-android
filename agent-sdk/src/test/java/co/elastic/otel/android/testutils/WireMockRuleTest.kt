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
package co.elastic.otel.android.testutils

import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WireMockRuleTest {
    @Test
    fun `Server port is released after consumed requests`() {
        val rule = WireMockRule()
        var port = 0

        evaluate(rule) {
            val requests = rule.stubRequests().enqueueResponse { withBody("response") }
            val url = URI(rule.url("/")).toURL()
            port = url.port

            assertThat(url.readText()).isEqualTo("response")
            requests.takeNextRequest()
        }

        assertPortIsAvailable(port)
    }

    @Test
    fun `Unused requests fail validation without leaking the server port`() {
        val rule = WireMockRule()
        var port = 0

        val exception = assertThrows<IllegalStateException> {
            evaluate(rule) {
                rule.stubRequests().enqueueResponse { withBody("response") }
                val url = URI(rule.url("/")).toURL()
                port = url.port

                assertThat(url.readText()).isEqualTo("response")
            }
        }

        assertThat(exception).hasMessage("Not all requests were used")
        assertPortIsAvailable(port)
    }

    private fun evaluate(rule: WireMockRule, body: () -> Unit) {
        rule.apply(object : Statement() {
            override fun evaluate() = body()
        }, Description.EMPTY).evaluate()
    }

    private fun assertPortIsAvailable(port: Int) {
        ServerSocket().use {
            it.reuseAddress = true
            it.bind(InetSocketAddress(port))
        }
    }
}
