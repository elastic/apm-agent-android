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
package co.elastic.otel.android.functional

import co.elastic.otel.android.ElasticApmAgent
import co.elastic.otel.android.connectivity.Authentication
import co.elastic.otel.android.connectivity.ExportEndpointConfiguration
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.exporters.configuration.ExportProtocol
import co.elastic.otel.android.interceptor.Interceptor
import co.elastic.otel.android.internal.api.ManagedElasticOtelAgent
import co.elastic.otel.android.internal.features.centralconfig.CentralConfigurationConnectivity
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.test.exporter.InMemoryExporterProvider
import co.elastic.otel.android.test.processor.SimpleProcessorFactory
import co.elastic.otel.android.testutils.DummySntpClient
import co.elastic.otel.android.testutils.WireMockRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ElasticApmAgentTest {
    private lateinit var agent: ElasticApmAgent
    private lateinit var simpleProcessorFactory: SimpleProcessorFactory
    private val inMemoryExporters = InMemoryExporterProvider()
    private val inMemoryExportersInterceptor = Interceptor<ExporterProvider> { inMemoryExporters }

    @get:Rule
    val wireMockRule = WireMockRule()

    @Before
    fun setUp() {
        simpleProcessorFactory = SimpleProcessorFactory()
    }

    @After
    fun tearDown() {
        closeAgent()
    }

    private fun closeAgent() {
        try {
            agent.close()
            inMemoryExporters.reset()
        } catch (ignored: UninitializedPropertyAccessException) {
        }
    }

    @Test
    fun `Validate url is present`() {
        val exception = assertThrows<NullPointerException> {
            ElasticApmAgent.builder(RuntimeEnvironment.getApplication()).build()
        }

        assertThat(exception).hasMessage("The export URL must be set.")
    }

    @Test
    fun `Validate delegation`() {
        val delegate = mockk<ManagedElasticOtelAgent>()
        val flushMetricsResult = mockk<CompletableResultCode>()
        val flushLogRecordsResult = mockk<CompletableResultCode>()
        val flushSpansRecordsResult = mockk<CompletableResultCode>()
        val openTelemetry = mockk<OpenTelemetry>()
        every { delegate.getOpenTelemetry() }.returns(openTelemetry)
        every { delegate.flushMetrics() }.returns(flushMetricsResult)
        every { delegate.flushLogRecords() }.returns(flushLogRecordsResult)
        every { delegate.flushSpans() }.returns(flushSpansRecordsResult)
        every { delegate.openTelemetry }.returns(mockk())
        every { delegate.close() } just Runs

        val agent = ElasticApmAgent(
            delegate,
            mockk(relaxUnitFun = true),
            mockk(relaxUnitFun = true),
            mockk(relaxUnitFun = true)
        )

        assertThat(agent.getOpenTelemetry()).isEqualTo(openTelemetry)
        assertThat(agent.flushMetrics()).isEqualTo(flushMetricsResult)
        assertThat(agent.flushLogRecords()).isEqualTo(flushLogRecordsResult)
        assertThat(agent.flushSpans()).isEqualTo(flushSpansRecordsResult)
        agent.close()
        verify { delegate.close() }
    }

    @Test
    fun `Validate initial export server params`() {
        wireMockRule.stubAllHttpResponses { withStatus(500) }
        agent = simpleAgentBuilder(wireMockRule.url("/"))
            .setManagementUrl(wireMockRule.url("/remote/"))
            .setServiceName("my-app")
            .setExportHeadersInterceptor {
                mutableMapOf("Extra-header" to "extra value").apply {
                    putAll(it)
                }
            }
            .build()

        val centralConfigRequest = wireMockRule.takeRequest()
        assertThat(centralConfigRequest.url).isEqualTo("/remote/?service.name=my-app")
        assertThat(
            centralConfigRequest.headers.getHeader("Extra-header").isPresent
        ).isFalse()

        // OTel requests
        sendSpan()
        val tracesRequest = wireMockRule.takeRequest()
        assertThat(tracesRequest.url).isEqualTo("/v1/traces")
        assertThat(
            tracesRequest.headers.getHeader("User-Agent").firstValue()
        ).startsWith("OTel-OTLP-Exporter-Java/")
        assertThat(
            tracesRequest.headers.getHeader("Extra-header").firstValue()
        ).isEqualTo("extra value")

        sendLog()
        val logsRequest = wireMockRule.takeRequest()
        assertThat(logsRequest.url).isEqualTo("/v1/logs")
        assertThat(
            logsRequest.headers.getHeader("User-Agent").firstValue()
        ).startsWith("OTel-OTLP-Exporter-Java/")
        assertThat(
            logsRequest.headers.getHeader("Extra-header").firstValue()
        ).isEqualTo("extra value")

        sendMetric()
        val metricsRequest = wireMockRule.takeRequest()
        assertThat(metricsRequest.url).isEqualTo("/v1/metrics")
        assertThat(
            metricsRequest.headers.getHeader("User-Agent").firstValue()
        ).startsWith("OTel-OTLP-Exporter-Java/")
        assertThat(
            metricsRequest.headers.getHeader("Extra-header").firstValue()
        ).isEqualTo("extra value")
    }

    @Test
    fun `Validate changing endpoint config`() {
        wireMockRule.stubAllHttpResponses {
            withStatus(404)
                .withHeader("Cache-Control", "max-age=1")// 1 second to wait for the next poll.
        }
        val secretToken = "secret-token"
        val apiKey = "api-key"
        val headersInterceptorProvider =
            AtomicReference<Interceptor<Map<String, String>>>(Interceptor.noop())
        agent = simpleAgentBuilder(wireMockRule.url("/first/"))
            .setManagementUrl(wireMockRule.url("/management/"))
            .setExportAuthentication(Authentication.SecretToken(secretToken))
            .setExportHeadersInterceptor { headersInterceptorProvider.get().intercept(it) }
            .setManagementAuthentication(Authentication.ApiKey(apiKey))
            .setServiceName("my-app")
            .setDeploymentEnvironment("debug")
            .build()

        val centralConfigRequest = wireMockRule.takeRequest()
        assertThat(centralConfigRequest.url).isEqualTo("/management/?service.name=my-app&service.deployment=debug")
        assertThat(
            centralConfigRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("ApiKey $apiKey")

        wireMockRule.stubAllHttpResponses { withStatus(500) }
        sendSpan()
        val tracesRequest = wireMockRule.takeRequest()
        assertThat(tracesRequest.url).isEqualTo("/first/v1/traces")
        assertThat(
            tracesRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("Bearer $secretToken")

        sendLog()
        val logsRequest = wireMockRule.takeRequest()
        assertThat(logsRequest.url).isEqualTo("/first/v1/logs")
        assertThat(
            logsRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("Bearer $secretToken")

        sendMetric()
        val metricsRequest = wireMockRule.takeRequest()
        assertThat(metricsRequest.url).isEqualTo("/first/v1/metrics")
        assertThat(
            metricsRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("Bearer $secretToken")

        // Changing global config
        headersInterceptorProvider.set(Interceptor {
            mutableMapOf("Custom-Header" to "custom value").apply {
                putAll(it)
            }
        })
        agent.setExportEndpointConfiguration(
            ExportEndpointConfiguration(
                wireMockRule.url("/second/"),
                Authentication.ApiKey(apiKey),
                ExportProtocol.HTTP
            )
        )

        val centralConfigRequest2 = wireMockRule.takeRequest()
        assertThat(centralConfigRequest2.url).isEqualTo("/management/?service.name=my-app&service.deployment=debug")
        assertThat(centralConfigRequest2.headers.getHeader("Authorization").firstValue()).isEqualTo(
            "ApiKey $apiKey"
        )

        // OTel requests
        sendSpan()
        val tracesRequest2 = wireMockRule.takeRequest()
        assertThat(tracesRequest2.url).isEqualTo("/second/v1/traces")
        assertThat(
            tracesRequest2.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("ApiKey $apiKey")
        assertThat(
            tracesRequest2.headers.getHeader("Custom-Header").firstValue()
        ).isEqualTo("custom value")

        sendLog()
        val logsRequest2 = wireMockRule.takeRequest()
        assertThat(logsRequest2.url).isEqualTo("/second/v1/logs")
        assertThat(
            logsRequest2.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("ApiKey $apiKey")
        assertThat(
            logsRequest2.headers.getHeader("Custom-Header").firstValue()
        ).isEqualTo("custom value")

        sendMetric()
        val metricsRequest2 = wireMockRule.takeRequest()
        assertThat(metricsRequest2.url).isEqualTo("/second/v1/metrics")
        assertThat(
            metricsRequest2.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("ApiKey $apiKey")
        assertThat(
            metricsRequest2.headers.getHeader("Custom-Header").firstValue()
        ).isEqualTo("custom value")
    }

    @Test
    fun `Validate changing endpoint config after manually setting central config endpoint`() {
        wireMockRule.stubAllHttpResponses {
            withStatus(404)
                .withHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
        }// Central config poll
        val secretToken = "secret-token"
        val initialUrl = wireMockRule.url("/first/")
        val initialManagementUrl = wireMockRule.url("/management/")
        agent = simpleAgentBuilder(initialUrl)
            .setExportAuthentication(Authentication.SecretToken(secretToken))
            .setManagementUrl(initialManagementUrl)
            .setManagementAuthentication(Authentication.SecretToken(secretToken))
            .setServiceName("my-app")
            .setDeploymentEnvironment("debug")
            .build()

        val centralConfigRequest = wireMockRule.takeRequest()
        assertThat(centralConfigRequest.url).isEqualTo("/management/?service.name=my-app&service.deployment=debug")
        assertThat(
            centralConfigRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("Bearer $secretToken")

        // Setting central config value manually
        agent.getCentralConfigurationManager()!!.setConnectivityConfiguration(
            CentralConfigurationConnectivity(
                initialManagementUrl,
                Authentication.None,
                emptyMap(),
                "other-name",
                null
            )
        )

        // OTel requests
        wireMockRule.stubAllHttpResponses { withStatus(500) }
        sendSpan()
        val tracesRequest = wireMockRule.takeRequest()
        assertThat(tracesRequest.url).isEqualTo("/first/v1/traces")
        assertThat(
            tracesRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("Bearer $secretToken")

        sendLog()
        val logsRequest = wireMockRule.takeRequest()
        assertThat(logsRequest.url).isEqualTo("/first/v1/logs")
        assertThat(
            logsRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("Bearer $secretToken")

        sendMetric()
        val metricsRequest = wireMockRule.takeRequest()
        assertThat(metricsRequest.url).isEqualTo("/first/v1/metrics")
        assertThat(
            metricsRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("Bearer $secretToken")

        // Changing config
        val apiKey = "api-key"
        agent.getExportConnectivityManager().setEndpointConfiguration(
            ExportEndpointConfiguration(
                wireMockRule.url("/second/"),
                Authentication.ApiKey(apiKey),
                ExportProtocol.HTTP
            )
        )

        val centralConfigRequest2 = wireMockRule.takeRequest()
        assertThat(centralConfigRequest2.url).isEqualTo("/management/?service.name=other-name")
        assertThat(centralConfigRequest2.headers.getHeader("Authorization").isPresent).isFalse()

        // OTel requests
        sendSpan()
        val tracesRequest2 = wireMockRule.takeRequest()
        assertThat(tracesRequest2.url).isEqualTo("/second/v1/traces")
        assertThat(
            tracesRequest2.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("ApiKey $apiKey")

        sendLog()
        val logsRequest2 = wireMockRule.takeRequest()
        assertThat(logsRequest2.url).isEqualTo("/second/v1/logs")
        assertThat(
            logsRequest2.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("ApiKey $apiKey")

        sendMetric()
        val metricsRequest2 = wireMockRule.takeRequest()
        assertThat(metricsRequest2.url).isEqualTo("/second/v1/metrics")
        assertThat(
            metricsRequest2.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("ApiKey $apiKey")
    }

    @Test
    fun `Validate central configuration is disabled when the url is not provided`() {
        agent = inMemoryAgentBuilder(wireMockRule.url("/"))
            .build()

        sendSpan()
        sendLog()
        sendMetric()

        awaitForOpenGates()

        assertThat(wireMockRule.getRequestSize()).isEqualTo(0)
        assertThat(agent.getCentralConfigurationManager()).isNull()
    }

    @Test
    fun `Validate central configuration behavior`() {
        // First: Empty config
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withBody("{}")
                .withHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
        }

        agent = inMemoryAgentBuilder(wireMockRule.url("/"))
            .setManagementUrl(wireMockRule.url("/management/"))
            .build()

        wireMockRule.takeRequest() // Await for empty central config response
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration())

        sendSpan()
        sendLog()
        sendMetric()

        awaitForOpenGates()
        awaitForNonEmptyInMemorySignals()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        // Next: Config with recording set to false
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withBody("""{"recording":"false"}""")
        }

        inMemoryExporters.resetExporters()

        wireMockRule.takeRequest() // Await for central config response with recording=false
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration(recording = false))

        awaitForOpenGates()

        sendSpan()
        sendLog()
        sendMetric()

        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).isEmpty()

        // Ensure that the config was persisted
        closeAgent()
        wireMockRule.stubAllHttpResponses {
            withStatus(404)
                .withHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
        }
        agent = inMemoryAgentBuilder(wireMockRule.url("/"))
            .setManagementUrl(wireMockRule.url("/management/"))
            .build()

        sendSpan()
        sendLog()
        sendMetric()

        // Await for request and stub the next one
        wireMockRule.takeRequest()
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration(recording = false))

        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
                .withBody("""{"recording":"true"}""")
        }

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).isEmpty()

        // Verify recording true value
        wireMockRule.takeRequest() // Await for recording: true request.
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration(recording = true))

        // Stub for invalid config
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withBody("NOT_A_JSON")
        }

        sendSpan()
        sendLog()
        sendMetric()

        awaitForNonEmptyInMemorySignals()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        // Verify invalid config
        inMemoryExporters.resetExporters()
        wireMockRule.takeRequest() // Await for invalid config.
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration())

        sendSpan()
        sendLog()
        sendMetric()

        awaitForNonEmptyInMemorySignals()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `Verify sampling rate config`() {
        // First: Sample rate: 0.0 and recording false.
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withBody("""{"session_sample_rate":"0.0", "recording":"false"}""")
                .withHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
        }

        agent = inMemoryAgentBuilder(wireMockRule.url("/"))
            .setManagementUrl(wireMockRule.url("/config/"))
            .build()

        wireMockRule.takeRequest() // Await for central config response
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration(false, 0.0))

        agent.getSessionManager().clearSession()

        sendSpan()
        sendLog()
        sendMetric()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).isEmpty()

        // Next: Sample rate: 1.0 and recording false.
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withBody("""{"session_sample_rate":"1.0", "recording":"false"}""")
                .withHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
        }

        wireMockRule.takeRequest() // Await for central config response
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration(false, 1.0))

        agent.getSessionManager().clearSession()

        sendSpan()
        sendLog()
        sendMetric()

        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).isEmpty()

        // Next: Sample rate: 1.0 and recording true.
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withBody("""{"session_sample_rate":"1.0", "recording":"true"}""")
                .withHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
        }

        wireMockRule.takeRequest() // Await for central config response
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration(true, 1.0))

        agent.getSessionManager().clearSession()

        sendSpan()
        sendLog()
        sendMetric()

        awaitForNonEmptyInMemorySignals()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        // Next: Sample rate: 0.0 and recording true, before evaluating sample rate.
        inMemoryExporters.resetExporters()
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withBody("""{"session_sample_rate":"0.0", "recording":"true"}""")
        }

        wireMockRule.takeRequest() // Await for central config response
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration(true, 0.0))

        sendSpan()
        sendLog()
        sendMetric()

        awaitForNonEmptyInMemorySignals()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        // Force refresh session to trigger sampling rate evaluation
        agent.getSessionManager().clearSession()
        inMemoryExporters.resetExporters()

        sendSpan()
        sendLog()
        sendMetric()

        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).isEmpty()

        // Ensure that the config is persisted.
        closeAgent()
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
            withBody("Not a json")
        }
        agent = inMemoryAgentBuilder(wireMockRule.url("/"))
            .setManagementUrl(wireMockRule.url("/config/"))
            .build()

        sendSpan()
        sendLog()
        sendMetric()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).isEmpty()

        // When central config fails and the session gets reset, go with default behavior.
        wireMockRule.takeRequest()
        awaitForCentralConfigurationValues(ExpectedCentralConfiguration(true, 1.0))

        agent.getSessionManager().clearSession()
        inMemoryExporters.resetExporters()

        sendSpan()
        sendLog()
        sendMetric()

        awaitForNonEmptyInMemorySignals()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `Validate http span name change`() {
        agent = inMemoryAgentBuilder().build()

        awaitForOpenGates()

        sendSpan("Normal Span")
        sendSpan(
            "GET",
            Attributes.of(
                AttributeKey.stringKey("url.full"),
                "http://somehost.com/some/path?q=some%20query"
            )
        )
        sendSpan(
            "POST",
            Attributes.of(
                AttributeKey.stringKey("url.full"),
                "https://anotherhost.net:8080/some/path?q=elastic"
            )
        )
        sendSpan(
            "PUT",
            Attributes.of(
                AttributeKey.stringKey("url.full"),
                "http://127.0.0.1:8080/some/path"
            )
        )
        sendSpan(
            "GET with something else apart from the verb",
            Attributes.of(
                AttributeKey.stringKey("url.full"),
                "https://anotherhost.net:8080/some/path?q=elastic"
            )
        )

        val finishedSpanNames = inMemoryExporters.getFinishedSpans().map { it.name }
        assertThat(finishedSpanNames).containsExactlyInAnyOrder(
            "Normal Span",
            "GET somehost.com",
            "POST anotherhost.net:8080",
            "PUT 127.0.0.1:8080",
            "GET with something else apart from the verb",
        )
    }

    @Test
    fun `Validate http span name change disabled`() {
        agent = inMemoryAgentBuilder()
            .setHttpSpanInterceptor(null)
            .build()

        awaitForOpenGates()

        sendSpan("Normal Span")
        sendSpan(
            "GET",
            Attributes.of(
                AttributeKey.stringKey("url.full"),
                "http://somehost.com/some/path?q=some%20query"
            )
        )
        sendSpan(
            "POST",
            Attributes.of(
                AttributeKey.stringKey("url.full"),
                "https://anotherhost.net:8080/some/path?q=elastic"
            )
        )
        sendSpan(
            "PUT",
            Attributes.of(
                AttributeKey.stringKey("url.full"),
                "http://127.0.0.1:8080/some/path"
            )
        )
        sendSpan(
            "GET with something else apart from the verb",
            Attributes.of(
                AttributeKey.stringKey("url.full"),
                "https://anotherhost.net:8080/some/path?q=elastic"
            )
        )

        val finishedSpanNames = inMemoryExporters.getFinishedSpans().map { it.name }
        assertThat(finishedSpanNames).containsExactlyInAnyOrder(
            "Normal Span",
            "GET",
            "POST",
            "PUT",
            "GET with something else apart from the verb",
        )
    }

    private fun simpleAgentBuilder(url: String): ElasticApmAgent.Builder {
        return ElasticApmAgent.builder(RuntimeEnvironment.getApplication())
            .setProcessorFactory(simpleProcessorFactory)
            .setDiskBufferingConfiguration(DiskBufferingConfiguration.disabled())
            .setExportUrl(url)
            .apply {
                internalSntpClient = DummySntpClient()
            }
    }

    private fun inMemoryAgentBuilder(url: String = "http://none"): ElasticApmAgent.Builder {
        return simpleAgentBuilder(url)
            .apply {
                internalExporterProviderInterceptor = inMemoryExportersInterceptor
            }
    }

    private fun sendSpan(name: String = "span-name", attributes: Attributes = Attributes.empty()) {
        agent.getOpenTelemetry().getTracer("TestTracer")
            .spanBuilder(name)
            .setAllAttributes(attributes)
            .startSpan()
            .end()
    }

    private fun sendLog() {
        agent.getOpenTelemetry().logsBridge.get("LoggerScope").logRecordBuilder()
            .setBody("Log body").emit()
    }

    private fun sendMetric() {
        agent.getOpenTelemetry().getMeter("MeterScope")
            .counterBuilder("counter")
            .build()
            .add(1)
        simpleProcessorFactory.flushMetrics().join(1, TimeUnit.SECONDS)
    }

    private fun awaitForOpenGates(maxSecondsToWait: Int = 1) {
        try {
            await.atMost(Duration.ofSeconds(maxSecondsToWait.toLong())).until {
                agent.getExporterGateManager().allGatesAreOpen()
            }
        } catch (e: ConditionTimeoutException) {
            println(
                "Pending latches: \n${
                    agent.getExporterGateManager().getAllOpenLatches().joinToString("\n")
                }"
            )
            throw e
        }
    }

    private fun awaitForNonEmptyInMemorySignals(maxSecondsToWait: Int = 1) {
        try {
            await.atMost(Duration.ofSeconds(maxSecondsToWait.toLong())).until {
                inMemoryExporters.getFinishedSpans().isNotEmpty()
                        && inMemoryExporters.getFinishedLogRecords().isNotEmpty()
                        && inMemoryExporters.getFinishedMetrics().isNotEmpty()
            }
        } catch (e: ConditionTimeoutException) {
            println("Spans size: ${inMemoryExporters.getFinishedSpans().size}")
            println("Logs size: ${inMemoryExporters.getFinishedLogRecords().size}")
            println("Metrics size: ${inMemoryExporters.getFinishedMetrics().size}")
            throw e
        }
    }

    private fun awaitForCentralConfigurationValues(
        expectedValue: ExpectedCentralConfiguration,
        waitSeconds: Int = 1
    ) {
        val centralConfiguration =
            agent.getCentralConfigurationManager()!!.getCentralConfiguration()

        try {
            await.atMost(Duration.ofSeconds(waitSeconds.toLong())).until {
                centralConfiguration.getSessionSampleRate() == expectedValue.sessionSampleRate &&
                        centralConfiguration.isRecording() == expectedValue.recording
            }
        } catch (e: ConditionTimeoutException) {
            println(
                "Configuration: ${
                    ExpectedCentralConfiguration(
                        centralConfiguration.isRecording(),
                        centralConfiguration.getSessionSampleRate()
                    )
                }"
            )
            throw e
        }
    }

    private data class ExpectedCentralConfiguration(
        val recording: Boolean = true,
        val sessionSampleRate: Double = 1.0
    )
}