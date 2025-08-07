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
package co.elastic.otel.android.oteladapter.internal.delegate.logger.noop

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Value
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.context.Context
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
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
