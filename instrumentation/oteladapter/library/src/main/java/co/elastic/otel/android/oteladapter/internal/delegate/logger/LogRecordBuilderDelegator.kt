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
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.context.Context
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
class LogRecordBuilderDelegator(initialValue: ExtendedLogRecordBuilder) :
    Delegator<ExtendedLogRecordBuilder>(
        initialValue
    ), ExtendedLogRecordBuilder {

    override fun setTimestamp(
        timestamp: Long,
        unit: TimeUnit
    ): ExtendedLogRecordBuilder? {
        return getDelegate().setTimestamp(timestamp, unit)
    }

    override fun setTimestamp(instant: Instant): ExtendedLogRecordBuilder? {
        return getDelegate().setTimestamp(instant)
    }

    override fun setObservedTimestamp(
        timestamp: Long,
        unit: TimeUnit
    ): ExtendedLogRecordBuilder? {
        return getDelegate().setObservedTimestamp(timestamp, unit)
    }

    override fun setObservedTimestamp(instant: Instant): ExtendedLogRecordBuilder? {
        return getDelegate().setObservedTimestamp(instant)
    }

    override fun setContext(context: Context): ExtendedLogRecordBuilder? {
        return getDelegate().setContext(context)
    }

    override fun setSeverity(severity: Severity): ExtendedLogRecordBuilder? {
        return getDelegate().setSeverity(severity)
    }

    override fun setSeverityText(severityText: String): ExtendedLogRecordBuilder? {
        return getDelegate().setSeverityText(severityText)
    }

    override fun setBody(body: String): ExtendedLogRecordBuilder? {
        return getDelegate().setBody(body)
    }

    override fun setEventName(eventName: String): ExtendedLogRecordBuilder? {
        return getDelegate().setEventName(eventName)
    }

    override fun <T : Any?> setAttribute(
        key: AttributeKey<T?>,
        value: T?
    ): ExtendedLogRecordBuilder? {
        return getDelegate().setAttribute(key, value)
    }

    override fun <T : Any?> setAttribute(
        key: ExtendedAttributeKey<T?>?,
        value: T?
    ): ExtendedLogRecordBuilder? {
        return getDelegate().setAttribute(key, value)
    }

    override fun setException(throwable: Throwable?): ExtendedLogRecordBuilder? {
        return getDelegate().setException(throwable)
    }

    override fun emit() {
        getDelegate().emit()
    }

    override fun getNoopValue(): ExtendedLogRecordBuilder {
        return NoopLogRecordBuilder.INSTANCE
    }
}