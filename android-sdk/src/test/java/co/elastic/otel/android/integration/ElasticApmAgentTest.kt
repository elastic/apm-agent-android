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
package co.elastic.otel.android.integration

import android.content.Intent
import co.elastic.otel.android.ElasticApmAgent
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.features.apmserver.ApmServerAuthentication
import co.elastic.otel.android.features.apmserver.ApmServerConnectivity
import co.elastic.otel.android.interceptor.Interceptor
import co.elastic.otel.android.internal.features.centralconfig.CentralConfigurationConnectivity
import co.elastic.otel.android.internal.features.clock.ElasticClockBroadcastReceiver
import co.elastic.otel.android.internal.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.otel.android.internal.features.sessionmanager.SessionIdGenerator
import co.elastic.otel.android.internal.services.appinfo.AppInfoService
import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.internal.time.ntp.SntpClient
import co.elastic.otel.android.processors.ProcessorFactory
import co.elastic.otel.android.testutils.ElasticAgentRule
import co.elastic.otel.android.testutils.WireMockRule
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.io.File
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
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

        assertThat(exception).hasMessage("The url must be set.")
    }

    @Test
    fun `Validate initial apm server params`() {
        wireMockRule.stubAllHttpResponses { withStatus(500) }
        agent = simpleAgentBuilder(wireMockRule.url("/"))
            .setServiceName("my-app")
            .setExtraRequestHeaders(mapOf("Extra-header" to "extra value"))
            .build()

        val centralConfigRequest = wireMockRule.takeRequest()
        assertThat(centralConfigRequest.url).isEqualTo("/config/v1/agents?service.name=my-app")
        assertThat(
            centralConfigRequest.headers.getHeader("Extra-header").firstValue()
        ).isEqualTo("extra value")

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
        agent = simpleAgentBuilder(wireMockRule.url("/first/"))
            .setAuthentication(ApmServerAuthentication.SecretToken(secretToken))
            .setServiceName("my-app")
            .setDeploymentEnvironment("debug")
            .build()

        val centralConfigRequest = wireMockRule.takeRequest()
        assertThat(centralConfigRequest.url).isEqualTo("/first/config/v1/agents?service.name=my-app&service.deployment=debug")
        assertThat(
            centralConfigRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("Bearer $secretToken")

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
        val apiKey = "api-key"
        agent.setApmServerConnectivity(
            ApmServerConnectivity(
                wireMockRule.url("/second/"),
                ApmServerAuthentication.ApiKey(apiKey),
                mapOf("Custom-Header" to "custom value")
            )
        )

        val centralConfigRequest2 = wireMockRule.takeRequest()
        assertThat(centralConfigRequest2.url).isEqualTo("/second/config/v1/agents?service.name=my-app&service.deployment=debug")
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
        agent = simpleAgentBuilder(initialUrl)
            .setAuthentication(ApmServerAuthentication.SecretToken(secretToken))
            .setServiceName("my-app")
            .setDeploymentEnvironment("debug")
            .build()

        val centralConfigRequest = wireMockRule.takeRequest()
        assertThat(centralConfigRequest.url).isEqualTo("/first/config/v1/agents?service.name=my-app&service.deployment=debug")
        assertThat(
            centralConfigRequest.headers.getHeader("Authorization").firstValue()
        ).isEqualTo("Bearer $secretToken")

        // Setting central config value manually
        agent.getCentralConfigurationManager().setConnectivityConfiguration(
            CentralConfigurationConnectivity(
                initialUrl,
                "other-name",
                null,
                mapOf("Custom-Header" to "Example")
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
        agent.getApmServerConnectivityManager().setConnectivityConfiguration(
            ApmServerConnectivity(
                wireMockRule.url("/second/"),
                ApmServerAuthentication.ApiKey(apiKey),
                mapOf("Custom-Header" to "custom value")
            )
        )

        val centralConfigRequest2 = wireMockRule.takeRequest()
        assertThat(centralConfigRequest2.url).isEqualTo("/first/config/v1/agents?service.name=other-name")
        assertThat(centralConfigRequest2.headers.getHeader("Authorization").isPresent).isFalse()
        assertThat(centralConfigRequest2.headers.getHeader("Custom-Header").firstValue()).isEqualTo(
            "Example"
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
    fun `Validate central configuration behavior`() {
        // First: Empty config
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withBody("{}")
                .withHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
        }

        agent = inMemoryAgentBuilder(wireMockRule.url("/"))
            .build()

        wireMockRule.takeRequest() // Await for empty central config response

        sendSpan()
        sendLog()
        sendMetric()

        awaitForOpenGates()

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
            .build()

        sendSpan()
        sendLog()
        sendMetric()

        // Await for request and stub the next one
        wireMockRule.takeRequest()
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
        // Stub for invalid config
        wireMockRule.stubAllHttpResponses {
            withStatus(200)
                .withBody("NOT_A_JSON")
        }

        sendSpan()
        sendLog()
        sendMetric()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        // Verify invalid config
        inMemoryExporters.resetExporters()
        wireMockRule.takeRequest() // Await for invalid config.

        sendSpan()
        sendLog()
        sendMetric()

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
            .build()

        wireMockRule.takeRequest() // Await for central config response
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
        agent.getSessionManager().clearSession()

        sendSpan()
        sendLog()
        sendMetric()

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

        sendSpan()
        sendLog()
        sendMetric()

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
        agent = inMemoryAgentBuilder(wireMockRule.url("/")).build()

        sendSpan()
        sendLog()
        sendMetric()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).isEmpty()

        // When central config fails and the session gets reset, go with default behavior.
        wireMockRule.takeRequest()
        agent.getSessionManager().clearSession()
        inMemoryExporters.resetExporters()

        sendSpan()
        sendLog()
        sendMetric()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `Disk buffering enabled, happy path`() {
        val configuration = DiskBufferingConfiguration.enabled()
        configuration.maxFileAgeForWrite = 500
        configuration.minFileAgeForRead = 501
        agent = inMemoryAgentBuilder(diskBufferingConfiguration = configuration)
            .build()

        sendSpan()
        sendLog()
        sendMetric()

        awaitForCacheFileCreation(listOf("spans", "logs", "metrics"))

        // Nothing should have gotten exported because it was stored in disk.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).isEmpty()

        // Re-init
        closeAgent()
        agent = inMemoryAgentBuilder(diskBufferingConfiguration = configuration)
            .build()

        val waitTimeMillis2 = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
                    && inMemoryExporters.getFinishedLogRecords().isNotEmpty()
                    && inMemoryExporters.getFinishedMetrics().isNotEmpty()
        }
        assertThat(waitTimeMillis2).isLessThan(2000)

        // Now we should see the previously-stored signals exported.
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `Disk buffering enabled, when signals come in before init is finished`() {
        val configuration = DiskBufferingConfiguration.enabled()
        configuration.maxFileAgeForWrite = 500
        configuration.minFileAgeForRead = 501
        agent = inMemoryAgentBuilder(diskBufferingConfiguration = configuration)
            .build()

        sendSpan()
        sendLog()
        sendMetric()

        val waitTimeMillis = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
                    && inMemoryExporters.getFinishedLogRecords().isNotEmpty()
                    && inMemoryExporters.getFinishedMetrics().isNotEmpty()
        }
        assertThat(waitTimeMillis).isLessThan(1500)

        // We should see the previously-stored signals exported.
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `Disk buffering enabled with io exception`() {
        val appInfoService = mockk<AppInfoService>(relaxed = true)
        every {
            appInfoService.getCacheDir()
            appInfoService.getAvailableCacheSpace(any())
        }.throws(IOException())
        agent =
            inMemoryAgentBuilder(diskBufferingConfiguration = DiskBufferingConfiguration.enabled())
                .apply {
                    internalExporterProviderInterceptor = inMemoryExportersInterceptor
                    internalServiceManagerInterceptor = Interceptor {
                        val spy = spyk(it)
                        every { spy.getAppInfoService() }.returns(appInfoService)
                        spy
                    }
                }
                .build()

        awaitForOpenGates()

        sendSpan()
        sendLog()
        sendMetric()

        // The signals should have gotten exported right away.
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)
    }

    @Test
    fun `Disk buffering disabled`() {
        agent =
            inMemoryAgentBuilder(diskBufferingConfiguration = DiskBufferingConfiguration.disabled())
                .build()

        sendSpan()
        sendLog()
        sendMetric()

        awaitForOpenGates()

        val waitTimeMillis = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().size == 1 &&
                    inMemoryExporters.getFinishedLogRecords().size == 1 &&
                    inMemoryExporters.getFinishedMetrics().size == 1
        }

        // The signals should have gotten exported right away.
        assertThat(waitTimeMillis).isLessThan(1000)
    }

    @Test
    fun `Verify clock behavior`() {
        val localTimeReference = 1577836800000L
        val timeOffset = 500L
        val expectedCurrentTime = localTimeReference + timeOffset
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(0)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(localTimeReference) }.returns(
            SntpClient.Response.Success(timeOffset)
        )
        every { sntpClient.close() } just Runs
        agent = inMemoryAgentBuilder()
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        await.until {
            agent.getElasticClockManager().getTimeOffsetManager()
                .getTimeOffset() == expectedCurrentTime
        }

        sendSpan()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            expectedCurrentTime * 1_000_000
        )

        // Reset after almost 24h with unavailable ntp server.
        closeAgent()
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        currentTime.set(currentTime.get() + TimeUnit.HOURS.toMillis(24) - 1)
        agent = inMemoryAgentBuilder()
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        sendSpan()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            expectedCurrentTime * 1_000_000
        )

        // Forward time past 24h and trigger time sync
        currentTime.set(currentTime.get() + 1)
        inMemoryExporters.resetExporters()
        val elapsedTime = 1000L
        every { systemTimeProvider.getElapsedRealTime() }.returns(elapsedTime)
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        sendSpan()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            currentTime.get() * 1_000_000
        )

        // Ensuring cache was cleared.
        closeAgent()
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        currentTime.set(currentTime.get() + 1000)
        agent = inMemoryAgentBuilder()
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        sendSpan()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            currentTime.get() * 1_000_000
        )

        // Picking up new time when available
        inMemoryExporters.resetExporters()
        every { sntpClient.fetchTimeOffset(localTimeReference + elapsedTime) }.returns(
            SntpClient.Response.Success(timeOffset)
        )
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        sendSpan()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            (timeOffset + localTimeReference + elapsedTime) * 1_000_000
        )

        // Reset after 24h with unavailable ntp server.
        closeAgent()
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        currentTime.set(currentTime.get() + TimeUnit.HOURS.toMillis(24))
        agent = inMemoryAgentBuilder()
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        sendSpan()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            currentTime.get() * 1_000_000
        )

        // Restarting with remote time available.
        closeAgent()
        every { sntpClient.fetchTimeOffset(localTimeReference + elapsedTime) }.returns(
            SntpClient.Response.Success(timeOffset)
        )
        agent = inMemoryAgentBuilder()
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        sendSpan()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            (timeOffset + localTimeReference + elapsedTime) * 1_000_000
        )

        // Ensuring cache is cleared on reboot.
        closeAgent()
        triggerRebootBroadcast()
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        currentTime.set(currentTime.get() + 1000)
        agent = inMemoryAgentBuilder()
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        sendSpan()

        awaitForOpenGates()

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            currentTime.get() * 1_000_000
        )
    }

    private fun triggerRebootBroadcast() {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        val broadcastReceivers =
            RuntimeEnvironment.getApplication().packageManager.queryBroadcastReceivers(intent, 0)
        assertThat(broadcastReceivers).hasSize(1)
        ElasticClockBroadcastReceiver().onReceive(RuntimeEnvironment.getApplication(), intent)
    }

    @Test
    fun `Verify clock initialization behavior`() {
        val localTimeReference = 1577836800000L
        val timeOffset = 500L
        val expectedCurrentTime = localTimeReference + timeOffset
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(localTimeReference) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        ).andThen(
            SntpClient.Response.Success(timeOffset)
        )
        every { sntpClient.close() } just Runs
        agent = inMemoryAgentBuilder()
            .setSessionIdGenerator { "session-id" }
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
                internalWaitForClock = true
            }
            .build()

        sendSpan()
        sendLog()
        sendMetric()

        await.atMost(Duration.ofSeconds(1)).until {
            agent.getExporterGateManager().metricGateIsOpen()
        }

        // Spans and logs aren't exported yet.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        // Time gets eventually set.
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        await.until {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
        }
        await.until {
            inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }

        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(ElasticAgentRule.SPAN_DEFAULT_ATTRS)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData).hasTimestamp(expectedCurrentTime * 1_000_000)
            .hasObservedTimestamp(currentTime.get() * 1_000_000)
            .hasAttributes(ElasticAgentRule.LOG_DEFAULT_ATTRS)

        // Send new data just for fun
        inMemoryExporters.resetExporters()

        sendSpan()
        sendLog()
        sendMetric()

        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(ElasticAgentRule.SPAN_DEFAULT_ATTRS)
        assertThat(inMemoryExporters.getFinishedLogRecords().first())
            .hasTimestamp(0)
            .hasObservedTimestamp(expectedCurrentTime * 1_000_000)
            .hasAttributes(ElasticAgentRule.LOG_DEFAULT_ATTRS)
    }

    @Test
    fun `Verify clock initialization behavior when processor delays signals`() {
        val localTimeReference = 1577836800000L
        val timeOffset = 500L
        val expectedCurrentTime = localTimeReference + timeOffset
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(localTimeReference) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        ).andThen(
            SntpClient.Response.Success(timeOffset)
        )
        every { sntpClient.close() } just Runs
        agent = ElasticApmAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl("http://none")
            .setDiskBufferingConfiguration(DiskBufferingConfiguration.disabled())
            .setSessionIdGenerator { "session-id" }
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
                internalExporterProviderInterceptor = inMemoryExportersInterceptor
            }
            .build()

        sendSpan()
        sendLog()

        await.atMost(Duration.ofSeconds(1)).until {
            agent.getExporterGateManager().metricGateIsOpen()
        }

        // Spans and logs aren't exported yet.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()

        // Time gets eventually set.
        agent.getElasticClockManager().getTimeOffsetManager().sync()

        await.until {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
        }
        await.until {
            inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }

        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(ElasticAgentRule.SPAN_DEFAULT_ATTRS)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData).hasTimestamp(expectedCurrentTime * 1_000_000)
            .hasObservedTimestamp(currentTime.get() * 1_000_000)
            .hasAttributes(ElasticAgentRule.LOG_DEFAULT_ATTRS)

        // Send new data just for fun
        inMemoryExporters.resetExporters()

        sendSpan()
        sendLog()

        await.until {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
        }
        await.until {
            inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)

        assertThat(inMemoryExporters.getFinishedSpans().first()).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(ElasticAgentRule.SPAN_DEFAULT_ATTRS)
        assertThat(inMemoryExporters.getFinishedLogRecords().first())
            .hasTimestamp(0)
            .hasObservedTimestamp(expectedCurrentTime * 1_000_000)
            .hasAttributes(ElasticAgentRule.LOG_DEFAULT_ATTRS)
    }

    @Test
    fun `Verify clock initialization when the first signal items come after init is done`() {
        val localTimeReference = 1577836800000L
        val timeOffset = 500L
        val expectedCurrentTime = localTimeReference + timeOffset
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(localTimeReference) }.returns(
            SntpClient.Response.Success(timeOffset)
        )
        every { sntpClient.close() } just Runs
        agent = inMemoryAgentBuilder()
            .setSessionIdGenerator { "session-id" }
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
                internalWaitForClock = true
            }
            .build()

        await.until {
            agent.getElasticClockManager().getClock().now() == expectedCurrentTime * 1_000_000
        }

        sendSpan()
        sendLog()
        sendMetric()

        // Everything's exported right away
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(1)
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            expectedCurrentTime * 1_000_000
        ).hasAttributes(ElasticAgentRule.SPAN_DEFAULT_ATTRS)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData).hasTimestamp(0)
            .hasObservedTimestamp(expectedCurrentTime * 1_000_000)
            .hasAttributes(ElasticAgentRule.LOG_DEFAULT_ATTRS)
    }

    @Test
    fun `Verify clock initialization behavior, when latch waiting times out`() {
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        every { sntpClient.close() } just Runs
        agent = inMemoryAgentBuilder()
            .setSessionIdGenerator { "session-id" }
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
                internalWaitForClock = true
            }
            .build()

        await.atMost(Duration.ofSeconds(1)).until {
            agent.getExporterGateManager().metricGateIsOpen()
        }

        sendSpan()
        sendLog()
        sendMetric()

        // Spans and logs aren't exported yet.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()
        assertThat(inMemoryExporters.getFinishedMetrics()).hasSize(1)

        val waitTimeMillis = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
                    && inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }

        assertThat(waitTimeMillis).isBetween(2500, 3500)
        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            currentTime.get() * 1_000_000
        ).hasAttributes(ElasticAgentRule.SPAN_DEFAULT_ATTRS)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData)
            .hasTimestamp(0)
            .hasObservedTimestamp(currentTime.get() * 1_000_000)
            .hasAttributes(ElasticAgentRule.LOG_DEFAULT_ATTRS)
    }

    @Test
    fun `Verify clock initialization behavior, when buffer gets full`() {
        val sntpClient = mockk<SntpClient>()
        val currentTime = AtomicLong(12345)
        val systemTimeProvider = spyk(SystemTimeProvider())
        val bufferSize = 10
        every { systemTimeProvider.getCurrentTimeMillis() }.answers {
            currentTime.get()
        }
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER)
        )
        every { sntpClient.close() } just Runs
        agent = inMemoryAgentBuilder()
            .setSessionIdGenerator { "session-id" }
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
                internalSignalBufferSize = bufferSize
                internalWaitForClock = true
            }
            .build()

        repeat(bufferSize) {
            sendSpan()
            sendLog()
        }

        // Spans and logs aren't exported yet.
        assertThat(inMemoryExporters.getFinishedSpans()).isEmpty()
        assertThat(inMemoryExporters.getFinishedLogRecords()).isEmpty()

        // Sending one more to go over the buffer size.
        sendSpan()
        sendLog()

        val waitTimeMillis = awaitAndTrackTimeMillis {
            inMemoryExporters.getFinishedSpans().isNotEmpty()
                    && inMemoryExporters.getFinishedLogRecords().isNotEmpty()
        }

        assertThat(waitTimeMillis).isLessThan(1000)
        assertThat(inMemoryExporters.getFinishedSpans()).hasSize(bufferSize + 1)
        assertThat(inMemoryExporters.getFinishedLogRecords()).hasSize(bufferSize + 1)
        val spanData = inMemoryExporters.getFinishedSpans().first()
        assertThat(spanData).startsAt(
            currentTime.get() * 1_000_000
        ).hasAttributes(ElasticAgentRule.SPAN_DEFAULT_ATTRS)
        val logRecordData = inMemoryExporters.getFinishedLogRecords().first()
        assertThat(logRecordData)
            .hasTimestamp(0)
            .hasObservedTimestamp(currentTime.get() * 1_000_000)
            .hasAttributes(ElasticAgentRule.LOG_DEFAULT_ATTRS)
    }

    @Test
    fun `Verify session manager behavior`() {
        val timeLimitMillis = TimeUnit.MINUTES.toMillis(30)
        val currentTimeMillis = AtomicLong(timeLimitMillis)
        val systemTimeProvider = spyk(SystemTimeProvider())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers { currentTimeMillis.get() }
        val sessionIdGenerator = mockk<SessionIdGenerator>()
        every { sessionIdGenerator.generate() }.returns("first-id")
        agent = inMemoryAgentBuilder()
            .setSessionIdGenerator { sessionIdGenerator.generate() }
            .apply {
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        awaitForOpenGates()

        sendSpan()
        sendLog()

        verifySessionId(inMemoryExporters.getFinishedSpans().first().attributes, "first-id")
        verifySessionId(
            inMemoryExporters.getFinishedLogRecords().first().attributes,
            "first-id"
        )
        verify(exactly = 1) { sessionIdGenerator.generate() }

        // Reset and verify that the id has been cached for just under the time limit.
        clearMocks(sessionIdGenerator)
        closeAgent()
        currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis - 1)
        agent = inMemoryAgentBuilder()
            .setSessionIdGenerator { sessionIdGenerator.generate() }
            .apply {
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        awaitForOpenGates()

        sendSpan()
        sendLog()

        verifySessionId(inMemoryExporters.getFinishedSpans().first().attributes, "first-id")
        verifySessionId(
            inMemoryExporters.getFinishedLogRecords().first().attributes,
            "first-id"
        )
        verify(exactly = 0) { sessionIdGenerator.generate() } // Was retrieved from cache.

        // Reset and verify that the id expires after an idle period of time
        clearMocks(sessionIdGenerator)
        every { sessionIdGenerator.generate() }.returns("second-id")
        inMemoryExporters.resetExporters()
        currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis - 1)

        sendSpan()
        sendLog()

        // Idle time passes
        currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis)

        sendSpan()
        sendLog()

        val finishedSpanItems = inMemoryExporters.getFinishedSpans()
        val finishedLogRecordItems = inMemoryExporters.getFinishedLogRecords()
        verifySessionId(finishedSpanItems.first().attributes, "first-id")
        verifySessionId(finishedSpanItems[1].attributes, "second-id")
        verifySessionId(finishedLogRecordItems.first().attributes, "first-id")
        verifySessionId(finishedLogRecordItems[1].attributes, "second-id")
        verify(exactly = 1) { sessionIdGenerator.generate() } // Was regenerated after idle time.

        // Reset and verify that the id expires 4 hours regardless of not enough idle time.
        clearMocks(sessionIdGenerator)
        every { sessionIdGenerator.generate() }.returns("third-id")
            .andThen("fourth-id")
        inMemoryExporters.resetExporters()

        repeat(18) { // each idle time 30 min
            currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis - 1)
            sendSpan()
        }

        val spanItems = inMemoryExporters.getFinishedSpans()
        var position = 0
        repeat(8) {
            verifySessionId(spanItems[position].attributes, "second-id")
            position++
        }
        repeat(8) {
            verifySessionId(spanItems[position].attributes, "third-id")
            position++
        }
        verifySessionId(spanItems[16].attributes, "third-id")
        verifySessionId(spanItems[17].attributes, "fourth-id")
    }

    private fun simpleAgentBuilder(
        url: String,
        diskBufferingConfiguration: DiskBufferingConfiguration = DiskBufferingConfiguration.disabled()
    ): ElasticApmAgent.Builder {
        return ElasticApmAgent.builder(RuntimeEnvironment.getApplication())
            .setProcessorFactory(simpleProcessorFactory)
            .setDiskBufferingConfiguration(diskBufferingConfiguration)
            .setUrl(url)
            .apply {
                internalWaitForClock = false
            }
    }

    private fun inMemoryAgentBuilder(
        url: String = "http://none",
        diskBufferingConfiguration: DiskBufferingConfiguration = DiskBufferingConfiguration.disabled()
    ): ElasticApmAgent.Builder {
        return simpleAgentBuilder(url, diskBufferingConfiguration)
            .apply {
                internalExporterProviderInterceptor = inMemoryExportersInterceptor
            }
    }

    private fun verifySessionId(attributes: Attributes, value: String) {
        assertThat(attributes.get(AttributeKey.stringKey("session.id"))).isEqualTo(value)
    }

    private fun sendSpan(name: String = "span-name") {
        agent.getOpenTelemetry().getTracer("TestTracer")
            .spanBuilder(name)
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
        simpleProcessorFactory.flush()
    }

    private fun awaitAndTrackTimeMillis(condition: () -> Boolean): Long {
        val waitStart = System.nanoTime()
        await.until(condition)
        val waitTimeMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - waitStart)
        return waitTimeMillis
    }

    private fun awaitForCacheFileCreation(dirNames: List<String>) {
        val signalsDir = File(RuntimeEnvironment.getApplication().cacheDir, "opentelemetry/signals")
        val dirs = dirNames.map { File(signalsDir, it) }
        await.until {
            var dirsNotEmpty = 0
            dirs.forEach {
                if (it.list().isNotEmpty()) {
                    dirsNotEmpty++
                }
            }
            dirsNotEmpty == dirs.size
        }
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

    private interface FlushableProcessorFactory : ProcessorFactory {
        fun flush()
    }

    private class SimpleProcessorFactory : FlushableProcessorFactory {
        private lateinit var metricReader: PeriodicMetricReader

        override fun createSpanProcessor(exporter: SpanExporter?): SpanProcessor? {
            return SimpleSpanProcessor.create(exporter)
        }

        override fun createLogRecordProcessor(exporter: LogRecordExporter?): LogRecordProcessor? {
            return SimpleLogRecordProcessor.create(exporter)
        }

        override fun createMetricReader(exporter: MetricExporter?): MetricReader {
            metricReader = PeriodicMetricReader.create(exporter)
            return metricReader
        }

        override fun flush() {
            metricReader.forceFlush()
        }
    }

    private class InMemoryExporterProvider : ExporterProvider {
        private var spanExporter = AtomicReference(InMemorySpanExporter.create())
        private var logRecordExporter = AtomicReference(InMemoryLogRecordExporter.create())
        private var metricExporter = AtomicReference(InMemoryMetricExporter.create())

        fun reset() {
            spanExporter.set(InMemorySpanExporter.create())
            logRecordExporter.set(InMemoryLogRecordExporter.create())
            metricExporter.set(InMemoryMetricExporter.create())
        }

        fun resetExporters() {
            spanExporter.get().reset()
            logRecordExporter.get().reset()
            metricExporter.get().reset()
        }

        fun getFinishedSpans(): List<SpanData> {
            return spanExporter.get().finishedSpanItems
        }

        fun getFinishedLogRecords(): List<LogRecordData> {
            return logRecordExporter.get().finishedLogRecordItems
        }

        fun getFinishedMetrics(): List<MetricData> {
            return metricExporter.get().finishedMetricItems
        }

        override fun getSpanExporter(): SpanExporter? {
            return spanExporter.get()
        }

        override fun getLogRecordExporter(): LogRecordExporter? {
            return logRecordExporter.get()
        }

        override fun getMetricExporter(): MetricExporter? {
            return metricExporter.get()
        }
    }
}