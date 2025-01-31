package co.elastic.otel.android.test

import android.app.Application
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import co.elastic.otel.android.ElasticApmAgent
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.processors.ProcessorFactory
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class AgentRule : TestRule {
    @Volatile
    private var agent: ElasticOtelAgent? = null

    @Volatile
    private var processorFactory: SimpleProcessorFactory? = null

    override fun apply(base: Statement, description: Description): Statement {
        return try {
            object : Statement() {
                override fun evaluate() {
                    processorFactory = SimpleProcessorFactory()
                    runOnUiThread {
                        agent =
                            ElasticApmAgent.builder(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application)
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

    private class SimpleProcessorFactory : ProcessorFactory {
        private val inMemoryMetricExporter = InMemoryMetricExporter.create()

        fun getFinishedMetrics(): List<MetricData> {
            return inMemoryMetricExporter.finishedMetricItems
        }

        override fun createSpanProcessor(exporter: SpanExporter?): SpanProcessor? {
            return SimpleSpanProcessor.create(exporter)
        }

        override fun createLogRecordProcessor(exporter: LogRecordExporter?): LogRecordProcessor? {
            return SimpleLogRecordProcessor.create(exporter)
        }

        override fun createMetricReader(exporter: MetricExporter?): MetricReader? {
            return PeriodicMetricReader.create(inMemoryMetricExporter)
        }
    }
}