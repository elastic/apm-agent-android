package co.elastic.otel.android.oteladapter.internal.delegate.logger

import co.elastic.otel.android.oteladapter.internal.delegate.logger.noop.NoopLoggerBuilder
import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tools.MultipleReference
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.LoggerBuilder

class LoggerBuilderDelegator(
    initialValue: LoggerBuilder,
    private val loggerReference: MultipleReference<Logger>
) : Delegator<LoggerBuilder>(initialValue), LoggerBuilder {

    override fun setSchemaUrl(schemaUrl: String): LoggerBuilder? {
        return getDelegate().setSchemaUrl(schemaUrl)
    }

    override fun setInstrumentationVersion(instrumentationScopeVersion: String): LoggerBuilder? {
        return getDelegate().setInstrumentationVersion(instrumentationScopeVersion)
    }

    override fun build(): Logger? {
        return loggerReference.maybeAdd(getDelegate().build())
    }

    override fun getNoopValue(): LoggerBuilder {
        return NoopLoggerBuilder.INSTANCE
    }
}