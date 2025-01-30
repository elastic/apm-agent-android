package co.elastic.otel.android.test

import android.app.Activity
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.elastic.otel.android.ElasticApmAgent
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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstrumentationTest {

    @Test
    fun verifyAppLaunchTimeTracking() {
        val processorFactory = SimpleProcessorFactory()

        ElasticApmAgent.builder(ApplicationProvider.getApplicationContext())
            .setUrl("http://none")
            .setProcessorFactory(processorFactory)
            .build()

        launchActivity<DummyActivity>().use {

        }
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

    class DummyActivity : Activity()
}