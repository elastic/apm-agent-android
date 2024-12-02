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

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import co.elastic.apm.android.sdk.internal.opentelemetry.clock.ElasticClock
import co.elastic.apm.android.sdk.testutils.ElasticAgentRule
import co.elastic.apm.android.sdk.testutils.ElasticAgentRule.Companion.LOG_DEFAULT_ATTRS
import co.elastic.apm.android.sdk.testutils.ElasticAgentRule.Companion.SPAN_DEFAULT_ATTRS
import io.mockk.every
import io.mockk.mockk
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.data.PointData
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
class AttributesTest {
    private lateinit var originalConstants: Map<String, String>

    @get:Rule
    val agentRule = ElasticAgentRule()

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

    @Config(sdk = [24, Config.NEWEST_SDK])
    @Test
    fun `Check resources`() {
        agentRule.initialize()
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

        agentRule.sendSpan()
        agentRule.sendLog()
        agentRule.sendMetricCounter()

        val spanItems = agentRule.getFinishedSpans()
        val logItems = agentRule.getFinishedLogRecords()
        val metricItems = agentRule.getFinishedMetrics()
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
        agentRule.initialize()

        agentRule.sendSpan()
        agentRule.sendLog()

        val spanItems = agentRule.getFinishedSpans()
        val logItems = agentRule.getFinishedLogRecords()
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(spanItems.first()).hasAttributes(SPAN_DEFAULT_ATTRS)
        assertThat(logItems.first()).hasAttributes(LOG_DEFAULT_ATTRS)
    }

    @Config(sdk = [24, Config.NEWEST_SDK])
    @Test
    fun `Check global attributes with cellular connectivity available`() {
        agentRule.initialize()
        enableCellularDataAttr()
        val expectedLogAttributes = Attributes.builder()
            .put("session.id", "session-id")
            .put("network.connection.type", "cell")
            .put("network.connection.subtype", "EDGE")
            .build()
        val expectedSpanAttributes = Attributes.builder()
            .putAll(expectedLogAttributes)
            .put("type", "mobile")
            .put("screen.name", "unknown")
            .build()

        agentRule.sendSpan()
        agentRule.sendLog()

        val spanItems = agentRule.getFinishedSpans()
        val logItems = agentRule.getFinishedLogRecords()
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(spanItems.first()).hasAttributes(expectedSpanAttributes)
        assertThat(logItems.first()).hasAttributes(expectedLogAttributes)
    }

    @Config(sdk = [24, Config.NEWEST_SDK])
    @Test
    fun `Check global attributes with carrier info available`() {
        agentRule.initialize()
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
            .put("screen.name", "unknown")
            .build()

        agentRule.sendSpan()
        agentRule.sendLog()

        val spanItems = agentRule.getFinishedSpans()
        val logItems = agentRule.getFinishedLogRecords()
        assertThat(spanItems).hasSize(1)
        assertThat(logItems).hasSize(1)
        assertThat(spanItems.first()).hasAttributes(expectedSpanAttributes)
        assertThat(logItems.first()).hasAttributes(expectedLogAttributes)
    }

    @Test
    fun `Check clock usage across all signals`() {
        val nowTime = 12345L
        agentRule.setElasticClockInterceptor {
            every { it.now() }.returns(nowTime)
            every { it.nanoTime() }.answers { System.nanoTime() }
        }
        agentRule.initialize()

        agentRule.sendSpan()
        agentRule.sendLog()
        agentRule.sendMetricCounter()

        assertThat(agentRule.getFinishedSpans().first().startEpochNanos).isEqualTo(nowTime)
        assertThat(agentRule.getFinishedLogRecords().first().observedTimestampEpochNanos).isEqualTo(
            nowTime
        )
        assertThat(getStartTime(agentRule.getFinishedMetrics().first())).isEqualTo(nowTime)
    }

    @Test
    fun `When timestamp changes mid-span, the end time shouldn't be affected`() {
        // This test is to verify that the internals of OTel Java use the system time nano
        // to track span start and end diff.

        val startTimeFromElasticClock = 2000000000L
        var clock: ElasticClock? = null
        agentRule.setElasticClockInterceptor {
            every { it.now() }.returns(startTimeFromElasticClock)
            every { it.nanoTime() }.answers { System.nanoTime() }
            clock = it
        }
        agentRule.initialize()

        val span = agentRule.openTelemetry.getTracer("SomeTracer").spanBuilder("TimeChangeSpan")
            .startSpan()

        // Moving now backwards:
        every { clock?.now() }.returns(1000000000L)

        span.end()

        val spanData = agentRule.getFinishedSpans().first()
        assertThat(spanData.startEpochNanos).isEqualTo(startTimeFromElasticClock)
        assertThat(spanData.endEpochNanos).isGreaterThan(startTimeFromElasticClock)
    }

    private fun getStartTime(metric: MetricData): Long {
        val points: List<PointData> = java.util.ArrayList(metric.data.points)
        return points[0].epochNanos
    }

    private fun enableCellularDataAttr() {
        val application = RuntimeEnvironment.getApplication()
        Shadows.shadowOf(application)
            .grantPermissions(Manifest.permission.READ_PHONE_STATE)
        val shadowConnectivityManager =
            Shadows.shadowOf(application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)
        val shadowTelephonyManager =
            Shadows.shadowOf(application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?)
        val callbacks = ArrayList(shadowConnectivityManager.networkCallbacks)
        val defaultNetworkCallback = callbacks[0]

        val capabilities = mockk<NetworkCapabilities>()
        every {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } returns true
        shadowTelephonyManager.setDataNetworkType(TelephonyManager.NETWORK_TYPE_EDGE)

        defaultNetworkCallback.onCapabilitiesChanged(mockk<Network>(), capabilities)
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
}