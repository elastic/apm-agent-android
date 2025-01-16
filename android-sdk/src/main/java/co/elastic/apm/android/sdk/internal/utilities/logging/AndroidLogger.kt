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
package co.elastic.apm.android.sdk.internal.utilities.logging

import android.util.Log
import co.elastic.apm.android.common.internal.logging.BaseELogger
import co.elastic.apm.android.sdk.configuration.logging.LogLevel
import co.elastic.apm.android.sdk.configuration.logging.LoggingPolicy
import org.slf4j.event.Level

internal class AndroidLogger(tag: String?, private val policy: LoggingPolicy) : BaseELogger(tag) {

    override fun handleLoggingCall(level: Level, formattedMessage: String, throwable: Throwable) {
        when (level) {
            Level.ERROR -> Log.e(name, formattedMessage, throwable)
            Level.WARN -> Log.w(name, formattedMessage, throwable)
            Level.INFO -> Log.i(name, formattedMessage, throwable)
            Level.DEBUG -> Log.d(name, formattedMessage, throwable)
            Level.TRACE -> Log.v(name, formattedMessage, throwable)
        }
    }

    override fun isTraceEnabled(): Boolean {
        return checkIfEnabledFor(LogLevel.TRACE)
    }

    override fun isDebugEnabled(): Boolean {
        return checkIfEnabledFor(LogLevel.DEBUG)
    }

    override fun isInfoEnabled(): Boolean {
        return checkIfEnabledFor(LogLevel.INFO)
    }

    override fun isWarnEnabled(): Boolean {
        return checkIfEnabledFor(LogLevel.WARN)
    }

    override fun isErrorEnabled(): Boolean {
        return checkIfEnabledFor(LogLevel.ERROR)
    }

    private fun checkIfEnabledFor(logLevel: LogLevel): Boolean {
        if (!policy.isEnabled()) {
            return false
        }

        return logLevel.value >= policy.getMinimumLevel().value
    }
}
