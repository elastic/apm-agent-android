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

import android.os.Build
import co.elastic.apm.android.sdk.ElasticApmAgent
import co.elastic.apm.android.sdk.ElasticApmConfiguration
import co.elastic.apm.android.sdk.connectivity.Connectivity
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
class IntegrationTest : SignalConfiguration {
    private lateinit var spanExporter: InMemorySpanExporter
    private lateinit var metricsReader: MetricReader
    private lateinit var metricsExporter: InMemoryMetricExporter
    private lateinit var logsExporter: InMemoryLogRecordExporter
    private lateinit var originalConstants: Map<String, String>

    companion object {
        private const val RUNTIME_VERSION: String = "runtime-version"
        private const val DEVICE_MODEL_NAME: String = "Device model name"
        private const val DEVICE_MANUFACTURER: String = "Device manufacturer"
        private const val OS_BUILD: String = "OS Build"
    }

    @Before
    fun setUp() {
        spanExporter = InMemorySpanExporter.create()
        metricsExporter = InMemoryMetricExporter.create()
        logsExporter = InMemoryLogRecordExporter.create()
        originalConstants = mapOf(
            "MODEL" to Build.MODEL,
            "MANUFACTURER" to Build.MANUFACTURER,
            "VERSION.INCREMENTAL" to Build.VERSION.INCREMENTAL,
            "java.vm.version" to System.getProperty("java.vm.version")
        )
        ReflectionHelpers.setStaticField(Build::class.java, "MODEL", DEVICE_MODEL_NAME)
        ReflectionHelpers.setStaticField(Build::class.java, "MANUFACTURER", DEVICE_MANUFACTURER)
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "INCREMENTAL", OS_BUILD)
        System.setProperty("java.vm.version", RUNTIME_VERSION)
    }

    @After
    fun tearDown() {
        GlobalOpenTelemetry.resetForTest()
        ReflectionHelpers.setStaticField(Build::class.java, "MODEL", originalConstants["MODEL"])
        ReflectionHelpers.setStaticField(
            Build::class.java,
            "MANUFACTURER",
            originalConstants["MANUFACTURER"]
        )
        System.setProperty("java.vm.version", originalConstants["java.vm.version"])
    }

    @Config(sdk = [24])
    @Test
    fun `Check resources`() {
        val openTelemetry = getOtelInstance()

        openTelemetry.getTracer("SomeTracer").spanBuilder("SomeSpan").startSpan().end()

        val spanItems = spanExporter.finishedSpanItems
        Assertions.assertThat(spanItems).hasSize(1)
        OpenTelemetryAssertions.assertThat(spanItems.first())
            .hasResource(
                Resource.builder()
                    .put("deployment.environment", "test")
                    .put("device.id", "device-id")
                    .put("device.manufacturer", DEVICE_MANUFACTURER)
                    .put("device.model.identifier", DEVICE_MODEL_NAME)
                    .put("os.description", "Android 7.0, API level 24, BUILD $OS_BUILD")
                    .put("os.name", "Android")
                    .put("os.version", "7.0")
                    .put("process.runtime.name", "Android Runtime")
                    .put("process.runtime.version", RUNTIME_VERSION)
                    .put("service.build", 0)
                    .put("service.name", "service-name")
                    .put("service.version", "0.0.0")
                    .put("telemetry.sdk.language", "java")
                    .put("telemetry.sdk.name", "android")
                    .put("telemetry.sdk.version", System.getProperty("agent_version")!!)
                    .build()
            )
    }

    private fun getOtelInstance(): OpenTelemetry {
        ElasticApmAgent.initialize(
            RuntimeEnvironment.getApplication(), ElasticApmConfiguration.builder()
                .setServiceName("service-name")
                .setServiceVersion("0.0.0")
                .setDeploymentEnvironment("test")
                .setSignalConfiguration(this)
                .setDeviceIdGenerator { "device-id" }
                .build(),
            Connectivity.simple("http://localhost")
        )
        return GlobalOpenTelemetry.get()
    }

    override fun getSpanProcessor(): SpanProcessor {
        return SimpleSpanProcessor.create(spanExporter)
    }

    override fun getLogProcessor(): LogRecordProcessor {
        return SimpleLogRecordProcessor.create(logsExporter)
    }

    override fun getMetricReader(): MetricReader {
        metricsReader = PeriodicMetricReader.create(metricsExporter)
        return metricsReader
    }
}