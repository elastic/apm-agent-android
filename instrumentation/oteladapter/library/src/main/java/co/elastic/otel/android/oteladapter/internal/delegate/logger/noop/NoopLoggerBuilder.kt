package co.elastic.otel.android.oteladapter.internal.delegate.logger.noop

import co.elastic.otel.android.oteladapter.internal.delegate.logger.LoggerDelegator
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.LoggerBuilder

class NoopLoggerBuilder private constructor() : LoggerBuilder {

    override fun setSchemaUrl(schemaUrl: String): LoggerBuilder {
        return this
    }

    override fun setInstrumentationVersion(instrumentationVersion: String): LoggerBuilder {
        return this
    }

    override fun build(): Logger {
        return LoggerDelegator.Companion.NOOP_INSTANCE
    }

    companion object {
        val INSTANCE = NoopLoggerBuilder()
    }
}
