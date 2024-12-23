package co.elastic.apm.android.sdk.features.conditionaldrop

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import java.util.function.Predicate

internal class ConditionalDropMetricExporter(
    private val drop: Predicate<SignalType>,
    private val delegate: MetricExporter
) : MetricExporter {

    override fun getAggregationTemporality(instrumentType: InstrumentType): AggregationTemporality {
        return delegate.getAggregationTemporality(instrumentType)
    }

    override fun export(metrics: MutableCollection<MetricData>): CompletableResultCode {
        if (drop.test(SignalType.METRIC)) {
            return CompletableResultCode.ofSuccess()
        }
        return delegate.export(metrics)
    }

    override fun flush(): CompletableResultCode {
        return delegate.flush()
    }

    override fun shutdown(): CompletableResultCode {
        return delegate.shutdown()
    }
}