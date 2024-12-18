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

import co.elastic.apm.android.sdk.exporters.ExporterProvider
import co.elastic.apm.android.sdk.internal.api.ElasticOtelAgent
import co.elastic.apm.android.sdk.internal.services.kotlin.ServiceManager
import co.elastic.apm.android.sdk.processors.ProcessorFactory
import co.elastic.apm.android.sdk.session.Session
import co.elastic.apm.android.sdk.session.SessionProvider
import co.elastic.apm.android.sdk.tools.Interceptor
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.TimeUnit
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment

class ElasticAgentRule : TestRule, ExporterProvider, ProcessorFactory,
    Interceptor<ElasticOtelAgent.Configuration> {
    private var spanExporter: InMemorySpanExporter? = null
    private var metricReader: MetricReader? = null
    private var metricsExporter: InMemoryMetricExporter? = null
    private var logsExporter: InMemoryLogRecordExporter? = null
    var agent: ElasticOtelAgent? = null
    val openTelemetry: OpenTelemetry
        get() {
            return agent!!.getOpenTelemetry()
        }

    companion object {
        val LOG_DEFAULT_ATTRS: Attributes = Attributes.builder()
            .put("session.id", "session-id")
            .put("network.connection.type", "unavailable")
            .build()
        val SPAN_DEFAULT_ATTRS: Attributes = Attributes.builder()
            .putAll(LOG_DEFAULT_ATTRS)
            .put("type", "mobile")
            .build()
    }

    override fun apply(base: Statement, description: Description): Statement {
        try {
            return object : Statement() {
                override fun evaluate() {
                    base.evaluate()
                }
            }
        } finally {
            close()
        }
    }

    fun initialize(
        serviceName: String = "service-name",
        serviceVersion: String? = "0.0.0",
        deploymentEnvironment: String = "test",
        clock: Clock = Clock.getDefault(),
        sessionProvider: SessionProvider = SessionProvider { Session.create("session-id") },
        configurationInterceptor: Interceptor<ElasticOtelAgent.Configuration> = this,
        serviceManagerInterceptor: Interceptor<ServiceManager>? = null
    ) {
        spanExporter = InMemorySpanExporter.create()
        metricsExporter = InMemoryMetricExporter.create()
        logsExporter = InMemoryLogRecordExporter.create()

        val builder = TestElasticOtelAgent.builder(RuntimeEnvironment.getApplication())
            .setServiceName(serviceName)
            .setDeploymentEnvironment(deploymentEnvironment)
            .setDeviceIdProvider { "device-id" }
            .setSessionProvider(sessionProvider)
            .setClock(clock)
            .setExporterProvider(this)
            .setProcessorFactory(this)
            .apply {
                configurationInterceptors.add(configurationInterceptor)
                serviceManagerInterceptor?.let { interceptor ->
                    serviceManagerInterceptors.add(interceptor)
                }
            }

        if (serviceVersion != null) {
            builder.setServiceVersion(serviceVersion)
        }

        agent = builder.build()
    }

    fun sendLog(body: String = "", builderVisitor: LogRecordBuilder.() -> Unit = {}) {
        val logRecordBuilder =
            agent!!.getOpenTelemetry().logsBridge.get("LoggerScope").logRecordBuilder()
        builderVisitor(logRecordBuilder)
        logRecordBuilder.setBody(body).emit()
    }

    fun sendSpan(name: String = "SomeSpan", builderVisitor: SpanBuilder.() -> Unit = {}) {
        val spanBuilder = agent!!.getOpenTelemetry().getTracer("SomeTracer").spanBuilder(name)
        builderVisitor(spanBuilder)
        spanBuilder.startSpan().end()
    }

    fun sendMetricCounter(name: String = "Counter") {
        agent!!.getOpenTelemetry().getMeter("MeterScope").counterBuilder(name).build().add(1)
    }

    fun getFinishedSpans(): List<SpanData> {
        val list = ArrayList(spanExporter!!.finishedSpanItems)
        spanExporter!!.reset()
        return list
    }

    fun getFinishedLogRecords(): List<LogRecordData> {
        val list = ArrayList(logsExporter!!.finishedLogRecordItems)
        logsExporter!!.reset()
        return list
    }

    fun getFinishedMetrics(): List<MetricData> {
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

    override fun intercept(item: ElasticOtelAgent.Configuration): ElasticOtelAgent.Configuration {
        return item
    }

    fun close() {
        agent?.close()
        spanExporter = null
        logsExporter = null
        metricReader = null
        metricsExporter = null
    }
}