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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.any
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import java.time.Duration
import java.util.UUID
import org.awaitility.kotlin.await
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class WireMockRule : TestRule {
    private var wireMock: WireMockServer? = null
    private var allResponsesStubId: UUID? = null

    override fun apply(base: Statement, description: Description): Statement {
        try {
            return object : Statement() {
                override fun evaluate() {
                    base.evaluate()
                    wireMock?.stop()
                }
            }
        } finally {
            wireMock = null
            allResponsesStubId = null
        }
    }

    fun url(path: String): String {
        ensureStarted()
        return wireMock!!.url(path)
    }

    fun stubAllHttpResponses(
        responseVisitor: ResponseDefinitionBuilder.() -> Unit = {}
    ) {
        ensureStarted()
        wireMock!!.removeStub(allResponsesStubId)
        val mappingBuilder = any(anyUrl()).withId(allResponsesStubId)
        val response = aResponse()
        responseVisitor(response)
        mappingBuilder.willReturn(response)
        wireMock!!.stubFor(mappingBuilder)
    }

    private fun ensureStarted() {
        if (wireMock == null) {
            allResponsesStubId = UUID.randomUUID()
            wireMock = WireMockServer()
            wireMock?.start()
        }
    }

    fun takeRequest(awaitSeconds: Int = 2): LoggedRequest {
        await.atMost(Duration.ofSeconds(awaitSeconds.toLong()))
            .until { wireMock?.allServeEvents?.isNotEmpty() }
        val allServeEvents = wireMock?.allServeEvents
        assertThat(allServeEvents).hasSize(1)
        val serveEvent = allServeEvents?.first()
        wireMock?.removeServeEvent(serveEvent?.id)
        return serveEvent!!.request
    }
}