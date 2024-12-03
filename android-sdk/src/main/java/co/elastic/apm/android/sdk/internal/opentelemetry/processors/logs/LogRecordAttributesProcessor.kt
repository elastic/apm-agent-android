package co.elastic.apm.android.sdk.internal.opentelemetry.processors.logs

import co.elastic.apm.android.sdk.tools.Interceptor
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.ReadWriteLogRecord

internal class LogRecordAttributesProcessor(private val interceptor: Interceptor<Attributes>) :
    LogRecordProcessor {

    override fun onEmit(context: Context, logRecord: ReadWriteLogRecord) {
        logRecord.setAllAttributes(interceptor.intercept(logRecord.toLogRecordData().attributes))
    }
}