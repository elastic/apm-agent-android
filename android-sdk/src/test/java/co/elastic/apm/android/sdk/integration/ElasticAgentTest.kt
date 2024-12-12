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
import co.elastic.apm.android.sdk.features.apmserver.ApmServerAuthentication
import co.elastic.apm.android.sdk.features.apmserver.ApmServerConnectivity
import co.elastic.apm.android.sdk.features.centralconfig.CentralConfigurationConnectivity
import co.elastic.apm.android.sdk.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.apm.android.sdk.features.sessionmanager.SessionIdGenerator
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import co.elastic.apm.android.sdk.processors.ProcessorFactory
import co.elastic.apm.android.sdk.tools.Interceptor
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
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.fail
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ElasticAgentTest {
    private lateinit var webServer: MockWebServer
    private lateinit var simpleProcessorFactory: SimpleProcessorFactory
    private lateinit var agent: ElasticAgent

    @Before
    fun setUp() {
        webServer = MockWebServer()
        webServer.start()
        simpleProcessorFactory = SimpleProcessorFactory()
    }

    @After
    fun tearDown() {
        webServer.close()
    }

    @Test
    fun `Validate initial apm server params`() {
        webServer.enqueue(MockResponse().setResponseCode(500))// Central config poll
        agent = ElasticAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl(webServer.url("/").toString())
            .setServiceName("my-app")
            .setDiskBufferingConfiguration(DiskBufferingConfiguration.disabled())
            .setProcessorFactory(simpleProcessorFactory)
            .setExtraRequestHeaders(mapOf("Extra-header" to "extra value"))
            .build()

        val centralConfigRequest = takeRequest()
        assertThat(centralConfigRequest.path).isEqualTo("/config/v1/agents?service.name=my-app")
        assertThat(centralConfigRequest.headers["Extra-header"]).isEqualTo("extra value")

        // OTel requests
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))

        sendSpan()
        val tracesRequest = takeRequest()
        assertThat(tracesRequest.path).isEqualTo("/v1/traces")
        assertThat(tracesRequest.headers["User-Agent"]).startsWith("OTel-OTLP-Exporter-Java/")
        assertThat(tracesRequest.headers["Extra-header"]).isEqualTo("extra value")

        sendLog()
        val logsRequest = takeRequest()
        assertThat(logsRequest.path).isEqualTo("/v1/logs")
        assertThat(logsRequest.headers["User-Agent"]).startsWith("OTel-OTLP-Exporter-Java/")
        assertThat(logsRequest.headers["Extra-header"]).isEqualTo("extra value")

        sendMetric()
        val metricsRequest = takeRequest()
        assertThat(metricsRequest.path).isEqualTo("/v1/metrics")
        assertThat(metricsRequest.headers["User-Agent"]).startsWith("OTel-OTLP-Exporter-Java/")
        assertThat(metricsRequest.headers["Extra-header"]).isEqualTo("extra value")

        agent.close()
    }

    @Test
    fun `Validate changing endpoint config`() {
        webServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .addHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
        )// Central config poll
        val secretToken = "secret-token"
        agent = ElasticAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl(webServer.url("/first/").toString())
            .setAuthentication(ApmServerAuthentication.SecretToken(secretToken))
            .setServiceName("my-app")
            .setDeploymentEnvironment("debug")
            .setDiskBufferingConfiguration(DiskBufferingConfiguration.disabled())
            .setProcessorFactory(simpleProcessorFactory)
            .build()

        val centralConfigRequest = takeRequest()
        assertThat(centralConfigRequest.path).isEqualTo("/first/config/v1/agents?service.name=my-app&service.deployment=debug")
        assertThat(centralConfigRequest.headers["Authorization"]).isEqualTo("Bearer $secretToken")

        // OTel requests
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))

        sendSpan()
        val tracesRequest = takeRequest()
        assertThat(tracesRequest.path).isEqualTo("/first/v1/traces")
        assertThat(tracesRequest.headers["Authorization"]).isEqualTo("Bearer $secretToken")

        sendLog()
        val logsRequest = takeRequest()
        assertThat(logsRequest.path).isEqualTo("/first/v1/logs")
        assertThat(logsRequest.headers["Authorization"]).isEqualTo("Bearer $secretToken")

        sendMetric()
        val metricsRequest = takeRequest()
        assertThat(metricsRequest.path).isEqualTo("/first/v1/metrics")
        assertThat(metricsRequest.headers["Authorization"]).isEqualTo("Bearer $secretToken")

        // Changing config
        val apiKey = "api-key"
        webServer.enqueue(MockResponse().setResponseCode(500))// Central config poll
        agent.getApmServerConnectivityManager().setConnectivityConfiguration(
            ApmServerConnectivity(
                webServer.url("/second/").toString(),
                ApmServerAuthentication.ApiKey(apiKey),
                mapOf("Custom-Header" to "custom value")
            )
        )

        val centralConfigRequest2 = takeRequest()
        assertThat(centralConfigRequest2.path).isEqualTo("/second/config/v1/agents?service.name=my-app&service.deployment=debug")
        assertThat(centralConfigRequest2.headers["Authorization"]).isEqualTo("ApiKey $apiKey")

        // OTel requests
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))

        sendSpan()
        val tracesRequest2 = takeRequest()
        assertThat(tracesRequest2.path).isEqualTo("/second/v1/traces")
        assertThat(tracesRequest2.headers["Authorization"]).isEqualTo("ApiKey $apiKey")
        assertThat(tracesRequest2.headers["Custom-Header"]).isEqualTo("custom value")

        sendLog()
        val logsRequest2 = takeRequest()
        assertThat(logsRequest2.path).isEqualTo("/second/v1/logs")
        assertThat(logsRequest2.headers["Authorization"]).isEqualTo("ApiKey $apiKey")
        assertThat(logsRequest2.headers["Custom-Header"]).isEqualTo("custom value")

        sendMetric()
        val metricsRequest2 = takeRequest()
        assertThat(metricsRequest2.path).isEqualTo("/second/v1/metrics")
        assertThat(metricsRequest2.headers["Authorization"]).isEqualTo("ApiKey $apiKey")
        assertThat(metricsRequest2.headers["Custom-Header"]).isEqualTo("custom value")

        agent.close()
    }

    @Test
    fun `Validate changing endpoint config after manually setting central config endpoint`() {
        webServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .addHeader("Cache-Control", "max-age=1") // 1 second to wait for the next poll.
        )// Central config poll
        val secretToken = "secret-token"
        val initialUrl = webServer.url("/first/").toString()
        agent = ElasticAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl(initialUrl)
            .setAuthentication(ApmServerAuthentication.SecretToken(secretToken))
            .setServiceName("my-app")
            .setDeploymentEnvironment("debug")
            .setDiskBufferingConfiguration(DiskBufferingConfiguration.disabled())
            .setProcessorFactory(simpleProcessorFactory)
            .build()

        val centralConfigRequest = takeRequest()
        assertThat(centralConfigRequest.path).isEqualTo("/first/config/v1/agents?service.name=my-app&service.deployment=debug")
        assertThat(centralConfigRequest.headers["Authorization"]).isEqualTo("Bearer $secretToken")

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
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))

        sendSpan()
        val tracesRequest = takeRequest()
        assertThat(tracesRequest.path).isEqualTo("/first/v1/traces")
        assertThat(tracesRequest.headers["Authorization"]).isEqualTo("Bearer $secretToken")

        sendLog()
        val logsRequest = takeRequest()
        assertThat(logsRequest.path).isEqualTo("/first/v1/logs")
        assertThat(logsRequest.headers["Authorization"]).isEqualTo("Bearer $secretToken")

        sendMetric()
        val metricsRequest = takeRequest()
        assertThat(metricsRequest.path).isEqualTo("/first/v1/metrics")
        assertThat(metricsRequest.headers["Authorization"]).isEqualTo("Bearer $secretToken")

        // Changing config
        val apiKey = "api-key"
        webServer.enqueue(MockResponse().setResponseCode(500))// Central config poll
        agent.getApmServerConnectivityManager().setConnectivityConfiguration(
            ApmServerConnectivity(
                webServer.url("/second/").toString(),
                ApmServerAuthentication.ApiKey(apiKey),
                mapOf("Custom-Header" to "custom value")
            )
        )

        val centralConfigRequest2 = takeRequest()
        assertThat(centralConfigRequest2.path).isEqualTo("/first/config/v1/agents?service.name=other-name")
        assertThat(centralConfigRequest2.headers["Authorization"]).isNull()
        assertThat(centralConfigRequest2.headers["Custom-Header"]).isEqualTo("Example")

        // OTel requests
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))
        webServer.enqueue(MockResponse().setResponseCode(500))

        sendSpan()
        val tracesRequest2 = takeRequest()
        assertThat(tracesRequest2.path).isEqualTo("/second/v1/traces")
        assertThat(tracesRequest2.headers["Authorization"]).isEqualTo("ApiKey $apiKey")
        assertThat(tracesRequest2.headers["Custom-Header"]).isEqualTo("custom value")

        sendLog()
        val logsRequest2 = takeRequest()
        assertThat(logsRequest2.path).isEqualTo("/second/v1/logs")
        assertThat(logsRequest2.headers["Authorization"]).isEqualTo("ApiKey $apiKey")
        assertThat(logsRequest2.headers["Custom-Header"]).isEqualTo("custom value")

        sendMetric()
        val metricsRequest2 = takeRequest()
        assertThat(metricsRequest2.path).isEqualTo("/second/v1/metrics")
        assertThat(metricsRequest2.headers["Authorization"]).isEqualTo("ApiKey $apiKey")
        assertThat(metricsRequest2.headers["Custom-Header"]).isEqualTo("custom value")

        agent.close()
    }

    @Test
    fun `Verify clock initialization`() {
        val localTime = 1577836800000L
        val timeOffset = 500L
        val expectedCurrentTime = localTime + timeOffset
        val sntpClient = mockk<SntpClient>()
        val systemTimeProvider = spyk(SystemTimeProvider.get())
        val spanExporter = InMemorySpanExporter.create()
        val fetchTimeLatch = CountDownLatch(1)
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
        every { sntpClient.fetchTimeOffset(localTime) }.answers {
            fetchTimeLatch.countDown()
            SntpClient.Response.Success(timeOffset)
        }
        every { sntpClient.close() } just Runs
        simpleProcessorFactory.spanExporterInterceptor = Interceptor {
            spanExporter
        }
        agent = ElasticAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl("http://none")
            .setProcessorFactory(simpleProcessorFactory)
            .apply {
                internalSntpClient = sntpClient
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        if (!fetchTimeLatch.await(5, TimeUnit.SECONDS)) {
            fail("Clock sync wait took too long.")
        }

        sendSpan()

        assertThat(spanExporter.finishedSpanItems.first().startEpochNanos).isEqualTo(
            expectedCurrentTime * 1_000_000
        )

        agent.close()
    }

    @Test
    fun `Verify session manager behavior`() {
        val timeLimitMillis = TimeUnit.MINUTES.toMillis(30)
        val currentTimeMillis = AtomicLong(timeLimitMillis)
        val systemTimeProvider = spyk(SystemTimeProvider.get())
        every { systemTimeProvider.getCurrentTimeMillis() }.answers { currentTimeMillis.get() }
        val sessionIdGenerator = mockk<SessionIdGenerator>()
        val spanExporter = AtomicReference(InMemorySpanExporter.create())
        val logRecordExporter = AtomicReference(InMemoryLogRecordExporter.create())
        every { sessionIdGenerator.generate() }.returns("first-id")
        simpleProcessorFactory.spanExporterInterceptor = Interceptor { spanExporter.get() }
        simpleProcessorFactory.logRecordExporterInterceptor =
            Interceptor { logRecordExporter.get() }
        agent = ElasticAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl("http://none")
            .setProcessorFactory(simpleProcessorFactory)
            .setSessionIdGenerator { sessionIdGenerator.generate() }
            .apply {
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        sendSpan()
        sendLog()

        verifySessionId(spanExporter.get().finishedSpanItems.first().attributes, "first-id")
        verifySessionId(
            logRecordExporter.get().finishedLogRecordItems.first().attributes,
            "first-id"
        )
        verify(exactly = 1) { sessionIdGenerator.generate() }

        // Reset and verify that the id has been cached for just under the time limit.
        clearMocks(sessionIdGenerator)
        spanExporter.set(InMemorySpanExporter.create())
        logRecordExporter.set(InMemoryLogRecordExporter.create())
        currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis - 1)
        agent.close()
        agent = ElasticAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl("http://none")
            .setProcessorFactory(simpleProcessorFactory)
            .setSessionIdGenerator { sessionIdGenerator.generate() }
            .apply {
                internalSystemTimeProvider = systemTimeProvider
            }
            .build()

        sendSpan()
        sendLog()

        verifySessionId(spanExporter.get().finishedSpanItems.first().attributes, "first-id")
        verifySessionId(
            logRecordExporter.get().finishedLogRecordItems.first().attributes,
            "first-id"
        )
        verify(exactly = 0) { sessionIdGenerator.generate() } // Was retrieved from cache.

        // Reset and verify that the id expires after an idle period of time
        clearMocks(sessionIdGenerator)
        every { sessionIdGenerator.generate() }.returns("second-id")
        spanExporter.get().reset()
        logRecordExporter.get().reset()
        currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis - 1)

        sendSpan()
        sendLog()

        // Idle time passes
        currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis)

        sendSpan()
        sendLog()

        verifySessionId(spanExporter.get().finishedSpanItems.first().attributes, "first-id")
        verifySessionId(spanExporter.get().finishedSpanItems[1].attributes, "second-id")
        verifySessionId(
            logRecordExporter.get().finishedLogRecordItems.first().attributes,
            "first-id"
        )
        verifySessionId(
            logRecordExporter.get().finishedLogRecordItems[1].attributes,
            "second-id"
        )
        verify(exactly = 1) { sessionIdGenerator.generate() } // Was regenerated after idle time.

        // Reset and verify that the id expires 4 hours regardless of not enough idle time.
        clearMocks(sessionIdGenerator)
        every { sessionIdGenerator.generate() }.returns("third-id")
            .andThen("fourth-id")
        spanExporter.get().reset()
        logRecordExporter.get().reset()

        repeat(18) { // each idle time 30 min
            currentTimeMillis.set(currentTimeMillis.get() + timeLimitMillis - 1)
            sendSpan()
        }

        val spanItems = spanExporter.get().finishedSpanItems
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

    private fun verifySessionId(attributes: Attributes, value: String) {
        assertThat(attributes.get(AttributeKey.stringKey("session.id"))).isEqualTo(value)
    }

    private fun takeRequest() = webServer.takeRequest(2, TimeUnit.SECONDS)!!

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

    private class SimpleProcessorFactory : ProcessorFactory {
        private lateinit var metricReader: PeriodicMetricReader
        var spanExporterInterceptor: Interceptor<SpanExporter>? = null
        var logRecordExporterInterceptor: Interceptor<LogRecordExporter>? = null

        override fun createSpanProcessor(exporter: SpanExporter?): SpanProcessor? {
            return SimpleSpanProcessor.create(
                spanExporterInterceptor?.intercept(exporter!!) ?: exporter
            )
        }

        override fun createLogRecordProcessor(exporter: LogRecordExporter?): LogRecordProcessor? {
            return SimpleLogRecordProcessor.create(
                logRecordExporterInterceptor?.intercept(exporter!!) ?: exporter
            )
        }

        override fun createMetricReader(exporter: MetricExporter?): MetricReader? {
            metricReader = PeriodicMetricReader.create(exporter)
            return metricReader
        }

        fun flush() {
            metricReader.forceFlush()
        }
    }
}