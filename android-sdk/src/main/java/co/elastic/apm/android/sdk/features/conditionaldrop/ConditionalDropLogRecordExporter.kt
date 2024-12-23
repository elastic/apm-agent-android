package co.elastic.apm.android.sdk.features.conditionaldrop

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import java.util.function.Predicate

internal class ConditionalDropLogRecordExporter(
    private val drop: Predicate<SignalType>,
    private val delegate: LogRecordExporter
) : LogRecordExporter {

    override fun export(logs: MutableCollection<LogRecordData>): CompletableResultCode {
        if (drop.test(SignalType.LOG)) {
            return CompletableResultCode.ofSuccess()
        }
        return delegate.export(logs)
    }

    override fun flush(): CompletableResultCode {
        return delegate.flush()
    }

    override fun shutdown(): CompletableResultCode {
        return delegate.shutdown()
    }
}