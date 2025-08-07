package co.elastic.otel.android.oteladapter.internal.delegate.logger

import co.elastic.otel.android.oteladapter.internal.delegate.logger.noop.NoopLogRecordBuilder
import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.context.Context
import java.time.Instant
import java.util.concurrent.TimeUnit

class LogRecordBuilderDelegator(initialValue: LogRecordBuilder) : Delegator<LogRecordBuilder>(
    initialValue
), LogRecordBuilder {

    override fun setTimestamp(
        timestamp: Long,
        unit: TimeUnit
    ): LogRecordBuilder? {
        return getDelegate().setTimestamp(timestamp, unit)
    }

    override fun setTimestamp(instant: Instant): LogRecordBuilder? {
        return getDelegate().setTimestamp(instant)
    }

    override fun setObservedTimestamp(
        timestamp: Long,
        unit: TimeUnit
    ): LogRecordBuilder? {
        return getDelegate().setObservedTimestamp(timestamp, unit)
    }

    override fun setObservedTimestamp(instant: Instant): LogRecordBuilder? {
        return getDelegate().setObservedTimestamp(instant)
    }

    override fun setContext(context: Context): LogRecordBuilder? {
        return getDelegate().setContext(context)
    }

    override fun setSeverity(severity: Severity): LogRecordBuilder? {
        return getDelegate().setSeverity(severity)
    }

    override fun setSeverityText(severityText: String): LogRecordBuilder? {
        return getDelegate().setSeverityText(severityText)
    }

    override fun setBody(body: String): LogRecordBuilder? {
        return getDelegate().setBody(body)
    }

    override fun <T : Any?> setAttribute(
        key: AttributeKey<T?>,
        value: T?
    ): LogRecordBuilder? {
        return getDelegate().setAttribute(key, value)
    }

    override fun emit() {
        getDelegate().emit()
    }

    override fun getNoopValue(): LogRecordBuilder {
        return NoopLogRecordBuilder.INSTANCE
    }
}