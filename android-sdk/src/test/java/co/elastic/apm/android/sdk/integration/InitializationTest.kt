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

import co.elastic.apm.android.sdk.connectivity.ExportProtocol
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.ExporterVisitor
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.VisitableExporters
import co.elastic.apm.android.sdk.features.persistence.PersistenceConfiguration
import co.elastic.apm.android.sdk.features.persistence.scheduler.ExportScheduler
import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager
import co.elastic.apm.android.sdk.internal.services.Service
import co.elastic.apm.android.sdk.internal.services.ServiceManager
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService
import co.elastic.apm.android.sdk.testutils.ElasticApmAgentRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InitializationTest {

    @get:Rule
    val agentRule = ElasticApmAgentRule()

    @Test
    fun `Verify defaults`() {
        agentRule.initialize()

        val persistenceInitializer = agentRule.persistenceInitializer
        verify(exactly = 0) { persistenceInitializer.prepare() }
        verify(exactly = 0) { persistenceInitializer.createSignalDiskExporter() }
        verify { agentRule.sessionManager.initialize() }
        verify { periodicWorkService.initialize() }
    }

    @Test
    fun `Central config initialization`() {
        val pollManager = mockk<ConfigurationPollManager>()
        agentRule.setCentralConfigurationInitializerInterceptor {
            every { it.pollManager }.returns(pollManager)
        }
        agentRule.initialize()

        verify { periodicWorkService.addTask(agentRule.centralConfigurationInitializer) }
        assertThat(ConfigurationPollManager.get()).isEqualTo(pollManager)
    }

    @Test
    fun `Persistence initialization`() {
        val exportScheduler = mockk<ExportScheduler>(relaxUnitFun = true)
        val visitableExporters = mockk<MockSignalConfiguration>()
        every { visitableExporters.setExporterVisitor(any()) } just Runs
        every { visitableExporters.spanProcessor }.returns(mockk(relaxUnitFun = true))
        every { visitableExporters.logProcessor }.returns(mockk(relaxUnitFun = true))
        every { visitableExporters.metricReader }.returns(mockk(relaxUnitFun = true))
        agentRule.setPersistenceInitializerInterceptor {
            every { it.prepare() } just Runs
            every { it.createSignalDiskExporter() }.returns(mockk())
        }
        agentRule.initialize(configurationInterceptor = {
            it.setPersistenceConfiguration(
                PersistenceConfiguration.builder()
                    .setExportScheduler(exportScheduler)
                    .setEnabled(true)
                    .build()
            )
            it.setSignalConfiguration(visitableExporters)
        })

        val persistenceInitializer = agentRule.persistenceInitializer

        verify { persistenceInitializer.prepare() }
        verify { persistenceInitializer.createSignalDiskExporter() }
        verify { exportScheduler.onPersistenceEnabled() }
        verify { visitableExporters.setExporterVisitor(persistenceInitializer) }
    }

    @Test
    fun `Not visiting exporters, disable persistence`() {
        val exportScheduler = mockk<ExportScheduler>(relaxUnitFun = true)
        agentRule.initialize(configurationInterceptor = {
            it.setPersistenceConfiguration(
                PersistenceConfiguration.builder()
                    .setExportScheduler(exportScheduler)
                    .setEnabled(true)
                    .build()
            )
        })

        val persistenceInitializer = agentRule.persistenceInitializer

        verify(exactly = 0) { persistenceInitializer.prepare() }
        verify(exactly = 0) { persistenceInitializer.createSignalDiskExporter() }
        verify { exportScheduler.onPersistenceDisabled() }
    }

    @Test
    fun `Verify default exporters`() {
        var spanExporter: SpanExporter? = null
        var metricExporter: MetricExporter? = null
        var logRecordExporter: LogRecordExporter? = null
        val visitor = object : ExporterVisitor {
            override fun <T : Any?> visitExporter(exporter: T): T {
                when (exporter) {
                    is SpanExporter -> spanExporter = exporter
                    is MetricExporter -> metricExporter = exporter
                    is LogRecordExporter -> logRecordExporter = exporter
                    else -> throw UnsupportedOperationException()
                }
                return exporter
            }
        }
        agentRule.initialize(configurationInterceptor = {
            it.setSignalConfiguration(
                SignalConfiguration.create().apply { setExporterVisitor(visitor) })
        })

        assertThat(spanExporter is OtlpGrpcSpanExporter).isTrue()
        assertThat(metricExporter is OtlpGrpcMetricExporter).isTrue()
        assertThat(logRecordExporter is OtlpGrpcLogRecordExporter).isTrue()
    }

    @Test
    fun `Verify http exporters`() {
        var spanExporter: SpanExporter? = null
        var metricExporter: MetricExporter? = null
        var logRecordExporter: LogRecordExporter? = null
        val visitor = object : ExporterVisitor {
            override fun <T : Any?> visitExporter(exporter: T): T {
                when (exporter) {
                    is SpanExporter -> spanExporter = exporter
                    is MetricExporter -> metricExporter = exporter
                    is LogRecordExporter -> logRecordExporter = exporter
                    else -> throw UnsupportedOperationException()
                }
                return exporter
            }
        }
        agentRule.initialize(configurationInterceptor = {
            it.setSignalConfiguration(
                SignalConfiguration.create().apply { setExporterVisitor(visitor) })
            it.setExportProtocol(ExportProtocol.HTTP)
        })

        assertThat(spanExporter is OtlpHttpSpanExporter).isTrue()
        assertThat(metricExporter is OtlpHttpMetricExporter).isTrue()
        assertThat(logRecordExporter is OtlpHttpLogRecordExporter).isTrue()
    }

    private class MockSignalConfiguration : SignalConfiguration, VisitableExporters {
        override fun getSpanProcessor(): SpanProcessor {
            throw UnsupportedOperationException()
        }

        override fun getLogProcessor(): LogRecordProcessor {
            throw UnsupportedOperationException()
        }

        override fun getMetricReader(): MetricReader {
            throw UnsupportedOperationException()
        }

        override fun setExporterVisitor(visitor: ExporterVisitor?) {
            throw UnsupportedOperationException()
        }
    }

    companion object {
        private val periodicWorkService: PeriodicWorkService
            get() = ServiceManager.get().getService(Service.Names.PERIODIC_WORK)
    }
}
