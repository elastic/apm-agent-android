package co.elastic.apm.android.sdk.features.conditionaldrop

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.function.Predicate

internal class ConditionalDropSpanExporter(
    private val drop: Predicate<SignalType>,
    private val delegate: SpanExporter
) : SpanExporter {

    override fun export(spans: MutableCollection<SpanData>): CompletableResultCode {
        if (drop.test(SignalType.SPAN)) {
            return CompletableResultCode.ofSuccess()
        }
        return delegate.export(spans)
    }

    override fun flush(): CompletableResultCode {
        return delegate.flush()
    }

    override fun shutdown(): CompletableResultCode {
        return delegate.shutdown()
    }
}