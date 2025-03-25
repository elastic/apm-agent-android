package co.elastic.otel.android.test.exporter

import co.elastic.otel.android.exporters.ExporterProvider
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.atomic.AtomicReference

class InMemoryExporterProvider : ExporterProvider {
    private var spanExporter = AtomicReference(InMemorySpanExporter.create())
    private var logRecordExporter = AtomicReference(InMemoryLogRecordExporter.create())
    private var metricExporter = AtomicReference(InMemoryMetricExporter.create())

    fun reset() {
        spanExporter.set(InMemorySpanExporter.create())
        logRecordExporter.set(InMemoryLogRecordExporter.create())
        metricExporter.set(InMemoryMetricExporter.create())
    }

    fun resetExporters() {
        spanExporter.get().reset()
        logRecordExporter.get().reset()
        metricExporter.get().reset()
    }

    fun getFinishedSpans(): List<SpanData> {
        return spanExporter.get().finishedSpanItems
    }

    fun getFinishedLogRecords(): List<LogRecordData> {
        return logRecordExporter.get().finishedLogRecordItems
    }

    fun getFinishedMetrics(): List<MetricData> {
        return metricExporter.get().finishedMetricItems
    }

    override fun getSpanExporter(): SpanExporter? {
        return spanExporter.get()
    }

    override fun getLogRecordExporter(): LogRecordExporter? {
        return logRecordExporter.get()
    }

    override fun getMetricExporter(): MetricExporter? {
        return metricExporter.get()
    }
}
