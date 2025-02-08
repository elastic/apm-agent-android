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

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.telephony.TelephonyManager
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.internal.opentelemetry.ElasticOpenTelemetry
import co.elastic.otel.android.internal.services.ServiceManager
import co.elastic.otel.android.internal.services.network.query.NetworkApi21QueryManager
import co.elastic.otel.android.internal.services.network.query.NetworkApi23QueryManager
import co.elastic.otel.android.internal.services.network.query.NetworkApi24QueryManager
import co.elastic.otel.android.processors.ProcessorFactory
import co.elastic.otel.android.session.Session
import co.elastic.otel.android.test.common.ElasticAttributes.getLogRecordDefaultAttributes
import co.elastic.otel.android.test.common.ElasticAttributes.getSpanDefaultAttributes
import io.mockk.every
import io.mockk.mockk
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.data.PointData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.data.StatusData
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
internal class AttributesTest : ExporterProvider, ProcessorFactory {
    private lateinit var originalConstants: Map<String, String>
    private var spanExporter: InMemorySpanExporter? = null
    private var metricReader: MetricReader? = null
    private var metricsExporter: InMemoryMetricExporter? = null
    private var logsExporter: InMemoryLogRecordExporter? = null
    private var elasticOpenTelemetry: ElasticOpenTelemetry? = null

    companion object {
        private const val RUNTIME_VERSION: String = "runtime-version"
        private const val DEVICE_MODEL_NAME: String = "Device model name"
        private const val DEVICE_MANUFACTURER: String = "Device manufacturer"
        private const val SIM_OPERATOR: String = "123456"
        private const val SIM_OPERATOR_NAME: String = "elasticphone"
        private const val SIM_COUNTRY_ISO: String = "us"
        private const val OS_BUILD: String = "OS Build"
        private const val VERSION_CODE: Long = 10
    }

    @Before
    fun setUp() {
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
        ReflectionHelpers.setStaticField(Build::class.java, "MODEL", originalConstants["MODEL"])
        ReflectionHelpers.setStaticField(
            Build::class.java,
            "MANUFACTURER",
            originalConstants["MANUFACTURER"]
        )
        System.setProperty("java.vm.version", originalConstants["java.vm.version"])
        setVersionCode(0)
    }

    @Config(sdk = [21, 23, Config.NEWEST_SDK])
    @Test
    fun `Check resources`() {
        initialize()
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

        sendSpan()
        sendLog()
        sendMetricCounter()

        val spanItems = getFinishedSpans()
        val logItems = getFinishedLogRecords()
        val metricItems = getFinishedMetrics()
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(metricItems).hasSize(1)
        assertThat(spanItems.first()).hasResource(expectedResource)
        assertThat(logItems.first()).hasResource(expectedResource)
        assertThat(metricItems.first()).hasResource(expectedResource)
    }

    @Config(sdk = [21, 23, Config.NEWEST_SDK])
    @Test
    fun `Check resources with not provided service version`() {
        setVersionName("1.2.3")
        initialize(serviceVersion = null)
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
            .put("service.version", "1.2.3")
            .put("telemetry.sdk.language", "java")
            .put("telemetry.sdk.name", "android")
            .put("telemetry.sdk.version", System.getProperty("agent_version")!!)
            .build()

        sendSpan()
        sendLog()
        sendMetricCounter()

        val spanItems = getFinishedSpans()
        val logItems = getFinishedLogRecords()
        val metricItems = getFinishedMetrics()
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(metricItems).hasSize(1)
        assertThat(spanItems.first()).hasResource(expectedResource)
        assertThat(logItems.first()).hasResource(expectedResource)
        assertThat(metricItems.first()).hasResource(expectedResource)
    }

    @Config(sdk = [21, 23, Config.NEWEST_SDK])
    @Test
    fun `Check global attributes and span status`() {
        initialize()

        sendSpan()
        sendLog()

        val spanItems = getFinishedSpans()
        val logItems = getFinishedLogRecords()
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(spanItems.first()).hasAttributes(getSpanDefaultAttributes())
            .hasStatus(StatusData.ok())
        assertThat(logItems.first()).hasAttributes(getLogRecordDefaultAttributes())
    }

    @Config(sdk = [21, 23, Config.NEWEST_SDK])
    @Test
    fun `Check global attributes with cellular connectivity available`() {
        initialize()
        enableCellularDataAttr()
        val expectedLogAttributes = Attributes.builder()
            .put("session.id", "session-id")
            .put("network.connection.type", "cell")
            .put("network.connection.subtype", "EDGE")
            .build()
        val expectedSpanAttributes = Attributes.builder()
            .putAll(expectedLogAttributes)
            .put("type", "mobile")
            .build()

        sendSpan()
        sendLog()

        val spanItems = getFinishedSpans()
        val logItems = getFinishedLogRecords()
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(spanItems.first()).hasAttributes(expectedSpanAttributes)
        assertThat(logItems.first()).hasAttributes(expectedLogAttributes)
    }

    @Config(sdk = [21, 23, Config.NEWEST_SDK])
    @Test
    fun `Check global attributes with carrier info available`() {
        initialize()
        enableCarrierInfoAttrs()
        val expectedLogAttributes = Attributes.builder()
            .put("session.id", "session-id")
            .put("network.connection.type", "unavailable")
            .build()
        val expectedSpanAttributes = Attributes.builder()
            .putAll(expectedLogAttributes)
            .put("network.carrier.mcc", "123")
            .put("network.carrier.mnc", "456")
            .put("network.carrier.name", "elasticphone")
            .put("network.carrier.icc", "us")
            .put("type", "mobile")
            .build()

        sendSpan()
        sendLog()

        val spanItems = getFinishedSpans()
        val logItems = getFinishedLogRecords()
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(spanItems.first()).hasAttributes(expectedSpanAttributes)
        assertThat(logItems.first()).hasAttributes(expectedLogAttributes)
    }

    @Test
    fun `Check clock usage across all signals`() {
        val nowTime = 12345L
        val clock = mockk<Clock>()
        every { clock.now() }.returns(nowTime)
        every { clock.nanoTime() }.answers { System.nanoTime() }
        initialize(clock = clock)

        sendSpan()
        sendLog()
        sendMetricCounter()

        assertThat(getFinishedSpans().first().startEpochNanos).isEqualTo(nowTime)
        assertThat(getFinishedLogRecords().first().observedTimestampEpochNanos).isEqualTo(
            nowTime
        )
        assertThat(getStartTime(getFinishedMetrics().first())).isEqualTo(nowTime)
    }

    @Test
    fun `When timestamp changes mid-span, the end time shouldn't be affected`() {
        // This test is to verify that the internals of OTel Java use the system time nano
        // to track span start and end diff.

        val startTimeFromElasticClock = 2000000000L
        val clock = mockk<Clock>()
        every { clock.now() }.returns(startTimeFromElasticClock)
        every { clock.nanoTime() }.answers { System.nanoTime() }
        initialize(clock = clock)

        val span = elasticOpenTelemetry!!.sdk.getTracer("SomeTracer").spanBuilder("TimeChangeSpan")
            .startSpan()

        // Moving now backwards:
        every { clock.now() }.returns(1000000000L)

        span.end()

        val spanData = getFinishedSpans().first()
        assertThat(spanData.startEpochNanos).isEqualTo(startTimeFromElasticClock)
        assertThat(spanData.endEpochNanos).isGreaterThan(startTimeFromElasticClock)
    }

    private fun getStartTime(metric: MetricData): Long {
        val points: List<PointData> = java.util.ArrayList(metric.data.points)
        return points[0].epochNanos
    }

    private fun enableCellularDataAttr() {
        val application = RuntimeEnvironment.getApplication()
        Shadows.shadowOf(application).run {
            grantPermissions(Manifest.permission.READ_PHONE_STATE)
            grantPermissions(Manifest.permission.ACCESS_NETWORK_STATE)
        }
        val connectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val shadowConnectivityManager =
            Shadows.shadowOf(connectivityManager)
        val shadowTelephonyManager =
            Shadows.shadowOf(application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
        val defaultNetworkCallback = shadowConnectivityManager.networkCallbacks.first()
        val activeNetworkInfo = mockk<NetworkInfo>()
        every { activeNetworkInfo.type }.returns(ConnectivityManager.TYPE_MOBILE)
        shadowConnectivityManager.setActiveNetworkInfo(activeNetworkInfo)
        var activeNetwork: Network = ShadowNetwork.newInstance(ConnectivityManager.TYPE_MOBILE)

        when (Build.VERSION.SDK_INT) {
            in 21..22 -> {
                assertThat(defaultNetworkCallback).isInstanceOf(NetworkApi21QueryManager::class.java)
                shadowTelephonyManager.setNetworkType(TelephonyManager.NETWORK_TYPE_EDGE)
            }

            23 -> {
                assertThat(defaultNetworkCallback).isInstanceOf(NetworkApi23QueryManager::class.java)
                shadowTelephonyManager.setNetworkType(TelephonyManager.NETWORK_TYPE_EDGE)
                activeNetwork = connectivityManager.activeNetwork!!
            }

            else -> {
                assertThat(defaultNetworkCallback).isInstanceOf(NetworkApi24QueryManager::class.java)
                shadowTelephonyManager.setDataNetworkType(TelephonyManager.NETWORK_TYPE_EDGE)
                activeNetwork = connectivityManager.activeNetwork!!
            }
        }

        val capabilities = mockk<NetworkCapabilities>()
        every {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } returns true

        defaultNetworkCallback.onCapabilitiesChanged(
            activeNetwork,
            capabilities
        )
    }

    private fun enableCarrierInfoAttrs() {
        val shadowTelephonyManager =
            Shadows.shadowOf(
                RuntimeEnvironment.getApplication()
                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
            )
        shadowTelephonyManager.setSimOperator(SIM_OPERATOR)
        shadowTelephonyManager.setSimState(TelephonyManager.SIM_STATE_READY)
        shadowTelephonyManager.setSimOperatorName(SIM_OPERATOR_NAME)
        shadowTelephonyManager.setSimCountryIso(SIM_COUNTRY_ISO)
    }

    private fun setVersionCode(versionCode: Long) {
        Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager)
            .getInternalMutablePackageInfo(RuntimeEnvironment.getApplication().packageName).versionCode =
            versionCode.toInt()
    }

    private fun setVersionName(versionName: String) {
        Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager)
            .getInternalMutablePackageInfo(RuntimeEnvironment.getApplication().packageName).versionName =
            versionName
    }

    private fun initialize(
        serviceName: String = "service-name",
        serviceVersion: String? = "0.0.0",
        deploymentEnvironment: String = "test",
        clock: Clock = Clock.getDefault()
    ) {
        val serviceManager = ServiceManager.create(RuntimeEnvironment.getApplication())
        val builder = ElasticOpenTelemetry.Builder()
            .setServiceName(serviceName)
            .setDeploymentEnvironment(deploymentEnvironment)
            .setClock(clock)
            .setSessionProvider { Session.create("session-id") }
            .setDeviceIdProvider { "device-id" }
        serviceVersion?.let { builder.setServiceVersion(it) }

        builder.setExporterProvider(this)
        builder.setProcessorFactory(this)

        spanExporter = InMemorySpanExporter.create()
        metricsExporter = InMemoryMetricExporter.create()
        logsExporter = InMemoryLogRecordExporter.create()
        elasticOpenTelemetry = builder.build(serviceManager)
    }

    private fun sendLog(body: String = "", builderVisitor: LogRecordBuilder.() -> Unit = {}) {
        val logRecordBuilder =
            elasticOpenTelemetry!!.sdk.logsBridge.get("LoggerScope").logRecordBuilder()
        builderVisitor(logRecordBuilder)
        logRecordBuilder.setBody(body).emit()
    }

    private fun sendSpan(name: String = "SomeSpan", builderVisitor: SpanBuilder.() -> Unit = {}) {
        val spanBuilder = elasticOpenTelemetry!!.sdk.getTracer("SomeTracer").spanBuilder(name)
        builderVisitor(spanBuilder)
        spanBuilder.startSpan().end()
    }

    private fun sendMetricCounter(name: String = "Counter") {
        elasticOpenTelemetry!!.sdk.getMeter("MeterScope").counterBuilder(name).build().add(1)
    }

    private fun getFinishedSpans(): List<SpanData> {
        val list = ArrayList(spanExporter!!.finishedSpanItems)
        spanExporter!!.reset()
        return list
    }

    private fun getFinishedLogRecords(): List<LogRecordData> {
        val list = ArrayList(logsExporter!!.finishedLogRecordItems)
        logsExporter!!.reset()
        return list
    }

    private fun getFinishedMetrics(): List<MetricData> {
        metricReader!!.forceFlush().join(1, TimeUnit.SECONDS)
        val list = ArrayList(metricsExporter!!.finishedMetricItems)
        metricsExporter!!.reset()
        return list
    }

    override fun getSpanExporter(): SpanExporter? {
        return spanExporter
    }

    override fun getLogRecordExporter(): LogRecordExporter? {
        return logsExporter
    }

    override fun getMetricExporter(): MetricExporter? {
        return metricsExporter
    }

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
}