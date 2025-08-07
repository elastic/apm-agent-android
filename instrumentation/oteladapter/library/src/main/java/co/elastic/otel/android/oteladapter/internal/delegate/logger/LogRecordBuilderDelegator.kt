/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.otel.android.oteladapter.internal.delegate.logger

import co.elastic.otel.android.oteladapter.internal.delegate.logger.noop.NoopLogRecordBuilder
import co.elastic.otel.android.oteladapter.internal.delegate.tools.Delegator
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.context.Context
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
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