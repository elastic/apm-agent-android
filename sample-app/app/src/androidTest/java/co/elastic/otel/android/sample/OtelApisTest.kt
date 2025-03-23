package co.elastic.otel.android.sample

import co.elastic.otel.android.test.exporter.InMemoryExporterProvider
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OtelApisTest {
    private lateinit var inMemoryExporterProvider: InMemoryExporterProvider
    private lateinit var spanProcessor: BatchSpanProcessor
    private lateinit var metricReader: PeriodicMetricReader
    private lateinit var logRecordProcessor: BatchLogRecordProcessor
    private lateinit var openTelemetry: OpenTelemetry

    @Before
    fun setUp() {
        inMemoryExporterProvider = InMemoryExporterProvider()
        spanProcessor =
            BatchSpanProcessor.builder(inMemoryExporterProvider.getSpanExporter()).build()
        metricReader = PeriodicMetricReader.create(inMemoryExporterProvider.getMetricExporter())
        logRecordProcessor =
            BatchLogRecordProcessor.builder(inMemoryExporterProvider.getLogRecordExporter()).build()

        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build())
            .setMeterProvider(SdkMeterProvider.builder().registerMetricReader(metricReader).build())
            .setLoggerProvider(
                SdkLoggerProvider.builder().addLogRecordProcessor(logRecordProcessor).build()
            )
            .build()
    }

    @Test
    fun checkSpans() {
        val span = openTelemetry.getTracer("spanscope").spanBuilder("mySpan")
            .setAllAttributes(Attributes.of(AttributeKey.stringKey("stringAttr"), "string value"))
            .startSpan()
        val scope = span.makeCurrent()
        scope.close()
        span.end()

        flushSpans()

        assertEquals(1, inMemoryExporterProvider.getFinishedSpans().size)
    }

    @Test
    fun checkLogs() {
        openTelemetry.logsBridge.get("logscope").logRecordBuilder().setBody("log body").emit()

        flushLogs()

        assertEquals(1, inMemoryExporterProvider.getFinishedLogRecords().size)
    }

    @Test
    fun checkMetrics() {
        val counter = openTelemetry.getMeter("metricscope").counterBuilder("counter").build()
        counter.add(1)

        flushMetrics()

        assertEquals(1, inMemoryExporterProvider.getFinishedMetrics().size)
    }

    private fun flushSpans() {
        spanProcessor.forceFlush().join(1, TimeUnit.SECONDS)
    }

    private fun flushLogs() {
        logRecordProcessor.forceFlush().join(1, TimeUnit.SECONDS)
    }

    private fun flushMetrics() {
        metricReader.forceFlush().join(1, TimeUnit.SECONDS)
    }
}