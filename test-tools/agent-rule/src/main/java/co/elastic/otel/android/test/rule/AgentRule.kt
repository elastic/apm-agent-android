package co.elastic.otel.android.test.rule

import android.app.Application
import co.elastic.otel.android.ElasticApmAgent
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.processors.ProcessorFactory
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
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

abstract class AgentRule : TestRule {
    @Volatile
    private var agent: ElasticOtelAgent? = null

    @Volatile
    private var processorFactory: SimpleProcessorFactory? = null

    override fun apply(base: Statement, description: Description): Statement {
        return try {
            object : Statement() {
                override fun evaluate() {
                    processorFactory = SimpleProcessorFactory()
                    runInitialization {
                        agent =
                            ElasticApmAgent.builder(it)
                                .setUrl("http://none")
                                .setProcessorFactory(processorFactory!!)
                                .build()
                    }
                    base.evaluate()
                }
            }
        } finally {
            agent = null
            processorFactory = null
        }
    }

    fun getFinishedMetrics(): List<MetricData> {
        return processorFactory!!.getFinishedMetrics()
    }

    fun getFinishedLogRecords(): List<LogRecordData> {
        return processorFactory!!.getFinishedLogRecords()
    }

    fun getFinishedSpans(): List<SpanData> {
        return processorFactory!!.getFinishedSpans()
    }

    private class SimpleProcessorFactory : ProcessorFactory {
        private val inMemoryMetricExporter = InMemoryMetricExporter.create()
        private val inMemoryLogRecordExporter = InMemoryLogRecordExporter.create()
        private val inMemorySpanExporter = InMemorySpanExporter.create()

        fun getFinishedMetrics(): List<MetricData> {
            return inMemoryMetricExporter.finishedMetricItems
        }

        fun getFinishedLogRecords(): List<LogRecordData> {
            return inMemoryLogRecordExporter.finishedLogRecordItems
        }

        fun getFinishedSpans(): List<SpanData> {
            return inMemorySpanExporter.finishedSpanItems
        }

        override fun createSpanProcessor(exporter: SpanExporter?): SpanProcessor? {
            return SimpleSpanProcessor.create(inMemorySpanExporter)
        }

        override fun createLogRecordProcessor(exporter: LogRecordExporter?): LogRecordProcessor? {
            return SimpleLogRecordProcessor.create(inMemoryLogRecordExporter)
        }

        override fun createMetricReader(exporter: MetricExporter?): MetricReader? {
            return PeriodicMetricReader.create(inMemoryMetricExporter)
        }
    }

    abstract fun runInitialization(initialization: (Application) -> Unit)
}