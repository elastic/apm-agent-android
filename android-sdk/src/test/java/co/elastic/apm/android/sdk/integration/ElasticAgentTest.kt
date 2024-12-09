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
import co.elastic.apm.android.sdk.exporters.apmserver.ApmServerAuthentication
import co.elastic.apm.android.sdk.exporters.apmserver.ApmServerConnectivityConfiguration
import co.elastic.apm.android.sdk.features.diskbuffering.DiskBufferingConfiguration
import co.elastic.apm.android.sdk.processors.ProcessorFactory
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
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
        agent = ElasticAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl(webServer.url("/").toString())
            .setServiceName("my-app")
            .setDiskBufferingConfiguration(DiskBufferingConfiguration.disabled())
            .setProcessorFactory(simpleProcessorFactory)
            .setExtraRequestHeaders(mapOf("Extra-header" to "extra value"))
            .build()

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
        val secretToken = "secret-token"
        agent = ElasticAgent.builder(RuntimeEnvironment.getApplication())
            .setUrl(webServer.url("/first/").toString())
            .setAuthentication(ApmServerAuthentication.SecretToken(secretToken))
            .setServiceName("my-app")
            .setDiskBufferingConfiguration(DiskBufferingConfiguration.disabled())
            .setProcessorFactory(simpleProcessorFactory)
            .build()

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
        agent.getApmServerConnectivityManager().setConnectivityConfiguration(
            ApmServerConnectivityConfiguration(
                webServer.url("/second/").toString(),
                ApmServerAuthentication.ApiKey(apiKey),
                mapOf("Custom-Header" to "custom value")
            )
        )

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

    private fun takeRequest() = webServer.takeRequest(1, TimeUnit.SECONDS)!!

    private fun sendSpan() {
        agent.getOpenTelemetry().getTracer("TestTracer")
            .spanBuilder("span-name")
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

        override fun createSpanProcessor(exporter: SpanExporter?): SpanProcessor? {
            return SimpleSpanProcessor.create(exporter)
        }

        override fun createLogRecordProcessor(exporter: LogRecordExporter?): LogRecordProcessor? {
            return SimpleLogRecordProcessor.create(exporter)
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