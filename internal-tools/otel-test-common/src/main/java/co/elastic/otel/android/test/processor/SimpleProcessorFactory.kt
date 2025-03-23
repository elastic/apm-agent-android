package co.elastic.otel.android.test.processor

import co.elastic.otel.android.api.flusher.MetricFlusher
import co.elastic.otel.android.processors.ProcessorFactory
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter

class SimpleProcessorFactory : ProcessorFactory, MetricFlusher {
    private lateinit var metricReader: PeriodicMetricReader

    override fun createSpanProcessor(exporter: SpanExporter?): SpanProcessor? {
        return SimpleSpanProcessor.create(exporter)
    }

    override fun createLogRecordProcessor(exporter: LogRecordExporter?): LogRecordProcessor? {
        return SimpleLogRecordProcessor.create(exporter)
    }

    override fun createMetricReader(exporter: MetricExporter?): MetricReader {
        metricReader = PeriodicMetricReader.create(exporter)
        return metricReader
    }

    override fun flushMetrics(): CompletableResultCode {
        return metricReader.forceFlush()
    }
}
