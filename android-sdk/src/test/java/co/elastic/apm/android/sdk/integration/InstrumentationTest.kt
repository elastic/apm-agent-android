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
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
class InstrumentationTest : SignalConfiguration {
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
        private const val VERSION_CODE: Long = 10
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
        setVersionCode(VERSION_CODE)
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
        setVersionCode(0)
    }

    @Config(sdk = [24, Config.NEWEST_SDK])
    @Test
    fun `Check resources`() {
        val openTelemetry = getOtelInstance()
        val expectedResource = Resource.builder()
            .put("deployment.environment", "test")
            .put("device.id", "device-id")
            .put("device.manufacturer", DEVICE_MANUFACTURER)
            .put("device.model.identifier", DEVICE_MODEL_NAME)
            .put(
                "os.description",
                "Android ${Build.VERSION.RELEASE}, API level ${Build.VERSION.SDK_INT}, BUILD $OS_BUILD"
            )
            .put("os.name", "Android")
            .put("os.version", Build.VERSION.RELEASE)
            .put("process.runtime.name", "Android Runtime")
            .put("process.runtime.version", RUNTIME_VERSION)
            .put("service.build", VERSION_CODE)
            .put("service.name", "service-name")
            .put("service.version", "0.0.0")
            .put("telemetry.sdk.language", "java")
            .put("telemetry.sdk.name", "android")
            .put("telemetry.sdk.version", System.getProperty("agent_version")!!)
            .build()

        openTelemetry.getTracer("SomeTracer").spanBuilder("SomeSpan").startSpan().end()
        openTelemetry.logsBridge.get("LoggerScope").logRecordBuilder().emit()
        openTelemetry.getMeter("MeterScope").counterBuilder("Counter").build().add(1)
        metricsReader.forceFlush()

        val spanItems = spanExporter.finishedSpanItems
        val logItems = logsExporter.finishedLogRecordItems
        val metricItems = metricsExporter.finishedMetricItems
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(metricItems).hasSize(1)
        assertThat(spanItems.first()).hasResource(expectedResource)
        assertThat(logItems.first()).hasResource(expectedResource)
        assertThat(metricItems.first()).hasResource(expectedResource)
    }

    @Config(sdk = [24, Config.NEWEST_SDK])
    @Test
    fun `Check global attributes`() {
        val openTelemetry = getOtelInstance()
        val expectedAttributes = Attributes.builder()
            .put("session.id", "session-id")
            .put("network.connection.type", "unavailable")
            .put("type", "mobile")
            .put("screen.name", "unknown")
            .build()

        openTelemetry.getTracer("SomeTracer").spanBuilder("SomeSpan").startSpan().end()
        openTelemetry.logsBridge.get("LoggerScope").logRecordBuilder().emit()

        val spanItems = spanExporter.finishedSpanItems
        val logItems = logsExporter.finishedLogRecordItems
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(spanItems.first()).hasAttributes(expectedAttributes)
        assertThat(logItems.first()).hasAttributes(expectedAttributes)
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

    private fun setVersionCode(versionCode: Long) {
        Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager)
            .getInternalMutablePackageInfo(RuntimeEnvironment.getApplication().packageName).versionCode =
            versionCode.toInt()
    }
}