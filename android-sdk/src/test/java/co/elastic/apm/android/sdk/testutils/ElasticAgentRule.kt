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
package co.elastic.apm.android.sdk.testutils

import co.elastic.apm.android.sdk.ElasticApmAgent
import co.elastic.apm.android.sdk.ElasticApmConfiguration
import co.elastic.apm.android.sdk.connectivity.Connectivity
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration
import co.elastic.apm.android.sdk.internal.configuration.Configuration
import co.elastic.apm.android.sdk.internal.configuration.provider.ConfigurationsProvider
import co.elastic.apm.android.sdk.internal.features.centralconfig.initializer.CentralConfigurationInitializer
import co.elastic.apm.android.sdk.internal.features.persistence.PersistenceInitializer
import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector
import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector.Interceptor
import co.elastic.apm.android.sdk.internal.opentelemetry.clock.ElasticClock
import co.elastic.apm.android.sdk.session.SessionManager
import io.mockk.spyk
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment

class ElasticAgentRule : TestRule, SignalConfiguration, AgentDependenciesInjector,
    ConfigurationsProvider {
    private lateinit var spanExporter: InMemorySpanExporter
    private lateinit var metricsReader: MetricReader
    private lateinit var metricsExporter: InMemoryMetricExporter
    private lateinit var logsExporter: InMemoryLogRecordExporter
    private var agentDependenciesInjector: AgentDependenciesInjector? = null
    private val configurations = mutableListOf<Configuration>()
    private var _openTelemetry: OpenTelemetry? = null
    val openTelemetry: OpenTelemetry
        get() {
            return _openTelemetry!!
        }

    override fun apply(base: Statement, description: Description): Statement {
        spanExporter = InMemorySpanExporter.create()
        metricsExporter = InMemoryMetricExporter.create()
        logsExporter = InMemoryLogRecordExporter.create()

        try {
            return object : Statement() {
                override fun evaluate() {
                    base.evaluate()
                }
            }
        } finally {
            ElasticApmAgent.resetForTest()
            GlobalOpenTelemetry.resetForTest()
            _openTelemetry = null
            agentDependenciesInjector = null
            configurations.clear()
        }
    }

    fun initialize(
        serviceName: String = "service-name",
        serviceVersion: String = "0.0.0",
        deploymentEnvironment: String = "test",
        configurationInterceptor: (ElasticApmConfiguration.Builder) -> Unit = {},
        connectivityInterceptor: (Connectivity) -> Connectivity = { it },
        interceptor: Interceptor? = null
    ) {
        val configBuilder = ElasticApmConfiguration.builder()
            .setServiceName(serviceName)
            .setServiceVersion(serviceVersion)
            .setDeploymentEnvironment(deploymentEnvironment)
            .setSignalConfiguration(this)
            .setDeviceIdGenerator { "device-id" }
            .setSessionIdGenerator { "session-id" }

        configurationInterceptor(configBuilder)

        ElasticApmAgent.initialize(
            RuntimeEnvironment.getApplication(),
            configBuilder.build(),
            connectivityInterceptor(Connectivity.simple("http://localhost"))
        ) {
            agentDependenciesInjector = interceptor?.intercept(it) ?: it
            configurations.addAll(agentDependenciesInjector!!.configurationsProvider.provideConfigurations())
            this
        }
        _openTelemetry = GlobalOpenTelemetry.get()
    }

    fun sendLog(body: String = "", builderVisitor: LogRecordBuilder.() -> Unit = {}) {
        val logRecordBuilder = openTelemetry.logsBridge.get("LoggerScope").logRecordBuilder()
        builderVisitor(logRecordBuilder)
        logRecordBuilder.setBody(body).emit()
    }

    fun sendSpan(name: String = "SomeSpan", builderVisitor: SpanBuilder.() -> Unit = {}) {
        val spanBuilder = openTelemetry.getTracer("SomeTracer").spanBuilder(name)
        builderVisitor(spanBuilder)
        spanBuilder.startSpan().end()
    }

    fun sendMetricCounter(name: String = "Counter") {
        openTelemetry.getMeter("MeterScope").counterBuilder(name).build().add(1)
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

    fun getFinishedSpans(): List<SpanData> {
        return spanExporter.finishedSpanItems
    }

    fun getFinishedLogRecords(): List<LogRecordData> {
        return logsExporter.finishedLogRecordItems
    }

    fun getFinishedMetrics(): List<MetricData> {
        metricsReader.forceFlush()
        return metricsExporter.finishedMetricItems
    }

    override fun getElasticClock(): ElasticClock {
        return agentDependenciesInjector!!.elasticClock
    }

    override fun getSessionManager(): SessionManager {
        return agentDependenciesInjector!!.sessionManager
    }

    override fun getCentralConfigurationInitializer(): CentralConfigurationInitializer {
        return agentDependenciesInjector!!.centralConfigurationInitializer
    }

    override fun getConfigurationsProvider(): ConfigurationsProvider {
        return this
    }

    override fun getPersistenceInitializer(): PersistenceInitializer {
        return agentDependenciesInjector!!.persistenceInitializer
    }

    override fun provideConfigurations(): MutableList<Configuration> {
        val spies = mutableListOf<Configuration>()
        for (configuration in configurations) {
            try {
                spies.add(spyk(configuration))
            } catch (ignored: IllegalArgumentException) {
                spies.add(configuration)
            }
        }
        return spies
    }
}