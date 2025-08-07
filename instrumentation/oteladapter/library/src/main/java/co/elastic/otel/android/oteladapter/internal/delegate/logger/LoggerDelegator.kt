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
import co.elastic.otel.android.oteladapter.internal.delegate.tools.MultipleReference
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.logs.Logger

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
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