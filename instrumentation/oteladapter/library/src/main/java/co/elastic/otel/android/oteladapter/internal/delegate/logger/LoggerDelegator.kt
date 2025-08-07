package co.elastic.otel.android.oteladapter.internal.delegate.logger

import co.elastic.otel.android.oteladapter.internal.delegate.logger.noop.NoopLogRecordBuilder
import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tools.MultipleReference
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.logs.Logger

class LoggerDelegator(initialValue: Logger) : Delegator<Logger>(initialValue), Logger {
    private val logRecordBuilderReference =
        MultipleReference<LogRecordBuilder>(NoopLogRecordBuilder.INSTANCE) {
            LogRecordBuilderDelegator(it)
        }

    override fun reset() {
        super.reset()
        logRecordBuilderReference.reset()
    }

    override fun logRecordBuilder(): LogRecordBuilder? {
        return logRecordBuilderReference.maybeAdd(getDelegate().logRecordBuilder())
    }

    override fun getNoopValue(): Logger {
        return NOOP_INSTANCE
    }

    companion object {
        val NOOP_INSTANCE = Logger { NoopLogRecordBuilder.INSTANCE }
    }
}