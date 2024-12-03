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

import co.elastic.apm.android.sdk.ElasticAgent
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import java.util.concurrent.TimeUnit
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ElasticAgentRule : TestRule {
    private lateinit var spanExporter: InMemorySpanExporter
    private lateinit var metricReader: MetricReader
    private lateinit var metricsExporter: InMemoryMetricExporter
    private lateinit var logsExporter: InMemoryLogRecordExporter
    private lateinit var agent: ElasticAgent
    val openTelemetry: OpenTelemetry
        get() {
            return agent.openTelemetry
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
            // Clean up
        }
    }

    fun initialize(
        serviceName: String = "service-name",
        serviceVersion: String = "0.0.0",
        deploymentEnvironment: String = "test",
        clock: Clock = Clock.getDefault()
    ) {
        metricReader = PeriodicMetricReader.create(metricsExporter)
        agent = ElasticAgent.builder()
            .setServiceName(serviceName)
            .setServiceVersion(serviceVersion)
            .setDeploymentEnvironment(deploymentEnvironment)
            .setServiceBuild(10)
            .setDeviceIdProvider { "device-id" }
            .setClock(clock)
            .setSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .setLogRecordProcessor(SimpleLogRecordProcessor.create(logsExporter))
            .setMetricReader(metricReader)
            .build()
    }

    fun sendLog(body: String = "", builderVisitor: LogRecordBuilder.() -> Unit = {}) {
        val logRecordBuilder =
            agent.openTelemetry.logsBridge.get("LoggerScope").logRecordBuilder()
        builderVisitor(logRecordBuilder)
        logRecordBuilder.setBody(body).emit()
    }

    fun sendSpan(name: String = "SomeSpan", builderVisitor: SpanBuilder.() -> Unit = {}) {
        val spanBuilder = agent.openTelemetry.getTracer("SomeTracer").spanBuilder(name)
        builderVisitor(spanBuilder)
        spanBuilder.startSpan().end()
    }

    fun sendMetricCounter(name: String = "Counter") {
        agent.openTelemetry.getMeter("MeterScope").counterBuilder(name).build().add(1)
    }

    fun getFinishedSpans(): List<SpanData> {
        val list = ArrayList(spanExporter.finishedSpanItems)
        spanExporter.reset()
        return list
    }

    fun getFinishedLogRecords(): List<LogRecordData> {
        val list = ArrayList(logsExporter.finishedLogRecordItems)
        logsExporter.reset()
        return list
    }

    fun getFinishedMetrics(): List<MetricData> {
        metricReader.forceFlush().join(1, TimeUnit.SECONDS)
        val list = ArrayList(metricsExporter.finishedMetricItems)
        metricsExporter.reset()
        return list
    }

}