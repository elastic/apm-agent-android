package co.elastic.otel.android.oteladapter.internal.delegate.logger

import co.elastic.otel.android.oteladapter.internal.delegate.logger.noop.NoopLoggerBuilder
import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import co.elastic.otel.android.oteladapter.internal.delegate.tools.MultipleReference
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.LoggerBuilder
import io.opentelemetry.api.logs.LoggerProvider

class LoggerProviderDelegator(initialValue: LoggerProvider) :
    Delegator<LoggerProvider>(initialValue), LoggerProvider {
    private val loggerReference = MultipleReference(LoggerDelegator.NOOP_INSTANCE) {
        LoggerDelegator(it)
    }
    private val loggerBuilderReference =
        MultipleReference<LoggerBuilder>(NoopLoggerBuilder.INSTANCE) {
            LoggerBuilderDelegator(it, loggerReference)
        }

    override fun reset() {
        super.reset()
        loggerReference.reset()
        loggerBuilderReference.reset()
    }

    override fun get(instrumentationScopeName: String): Logger? {
        return loggerReference.maybeAdd(getDelegate().get(instrumentationScopeName))
    }

    override fun loggerBuilder(instrumentationScopeName: String): LoggerBuilder? {
        return loggerBuilderReference.maybeAdd(getDelegate().loggerBuilder(instrumentationScopeName))
    }

    override fun getNoopValue(): LoggerProvider {
        return NOOP_INSTANCE
    }

    companion object {
        val NOOP_INSTANCE: LoggerProvider = LoggerProvider.noop()
    }
}