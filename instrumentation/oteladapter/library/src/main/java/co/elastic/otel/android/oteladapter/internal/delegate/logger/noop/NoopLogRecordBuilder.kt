package co.elastic.otel.android.oteladapter.internal.delegate.logger.noop

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Value
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.context.Context
import java.time.Instant
import java.util.concurrent.TimeUnit

class NoopLogRecordBuilder private constructor() : LogRecordBuilder {
    override fun setTimestamp(timestamp: Long, unit: TimeUnit): LogRecordBuilder {
        return this
    }

    override fun setTimestamp(instant: Instant): LogRecordBuilder {
        return this
    }

    override fun setObservedTimestamp(timestamp: Long, unit: TimeUnit): LogRecordBuilder {
        return this
    }

    override fun setObservedTimestamp(instant: Instant): LogRecordBuilder {
        return this
    }

    override fun setContext(context: Context): LogRecordBuilder {
        return this
    }

    override fun setSeverity(severity: Severity): LogRecordBuilder {
        return this
    }

    override fun setSeverityText(severityText: String): LogRecordBuilder {
        return this
    }

    override fun setBody(body: String): LogRecordBuilder {
        return this
    }

    override fun setBody(body: Value<*>): LogRecordBuilder {
        return this
    }

    override fun <T> setAttribute(key: AttributeKey<T?>, value: T?): LogRecordBuilder {
        return this
    }

    override fun emit() {}

    companion object {
        val INSTANCE = NoopLogRecordBuilder()
    }
}
