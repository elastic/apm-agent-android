package co.elastic.apm.android.sdk.features.diskbuffering

import co.elastic.apm.android.sdk.exporters.configurable.MutableLogRecordExporter
import co.elastic.apm.android.sdk.exporters.configurable.MutableMetricExporter
import co.elastic.apm.android.sdk.exporters.configurable.MutableSpanExporter
import co.elastic.apm.android.sdk.internal.services.re.ServiceManager
import io.opentelemetry.contrib.disk.buffering.SpanFromDiskExporter
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.export.SpanExporter

class DiskBufferingManager(private val configuration: DiskBufferingConfiguration) {
    private var spanExporter: MutableSpanExporter? = null
    private var logRecordExporter: MutableLogRecordExporter? = null
    private var metricExporter: MutableMetricExporter? = null
    private var interceptedSpanExporter: SpanExporter? = null
    private var interceptedLogRecordExporter: LogRecordExporter? = null
    private var interceptedMetricExporter: MetricExporter? = null
    private var signalFromDiskExporter: SignalFromDiskExporter? = null

    internal fun interceptSpanExporter(interceptedSpanExporter: SpanExporter): SpanExporter {
        this.interceptedSpanExporter = interceptedSpanExporter
        spanExporter = MutableSpanExporter()
        return spanExporter!!
    }

    internal fun interceptLogRecordExporter(interceptedLogRecordExporter: LogRecordExporter): LogRecordExporter {
        this.interceptedLogRecordExporter = interceptedLogRecordExporter
        logRecordExporter = MutableLogRecordExporter()
        return logRecordExporter!!
    }

    internal fun interceptMetricExporter(interceptedMetricExporter: MetricExporter): MetricExporter {
        this.interceptedMetricExporter = interceptedMetricExporter
        metricExporter = MutableMetricExporter()
        return metricExporter!!
    }

    internal fun initialize(serviceManager: ServiceManager) {
        val builder = SignalFromDiskExporter.builder()

        TODO()
        spanExporter?.let {
            builder.setSpanFromDiskExporter(SpanFromDiskExporter.create(interceptedSpanExporter!!))
        }
    }

    fun exportFromDisk() {
        signalFromDiskExporter.exportBatchOfEach()
    }
}