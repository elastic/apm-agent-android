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
package co.elastic.apm.android.sdk.internal.utilities.logging;

import android.util.Log;

import org.slf4j.event.Level;

import co.elastic.apm.android.common.internal.logging.BaseELogger;
import co.elastic.apm.android.sdk.configuration.logging.LogLevel;
import co.elastic.apm.android.sdk.configuration.logging.LoggingPolicy;

class AndroidLogger extends BaseELogger {
    private final LoggingPolicy policy;

    AndroidLogger(String tag, LoggingPolicy policy) {
        super(tag);
        this.policy = policy;
    }

    @Override
    protected void handleLoggingCall(Level level, String formattedMessage, Throwable throwable) {
        switch (level) {
            case ERROR:
                Log.e(name, formattedMessage, throwable);
                break;
            case WARN:
                Log.w(name, formattedMessage, throwable);
                break;
            case INFO:
                Log.i(name, formattedMessage, throwable);
                break;
            case DEBUG:
                Log.d(name, formattedMessage, throwable);
                break;
            case TRACE:
                Log.v(name, formattedMessage, throwable);
                break;
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return checkIfEnabledFor(LogLevel.TRACE);
    }

    @Override
    public boolean isDebugEnabled() {
        return checkIfEnabledFor(LogLevel.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return checkIfEnabledFor(LogLevel.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return checkIfEnabledFor(LogLevel.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return checkIfEnabledFor(LogLevel.ERROR);
    }

    private boolean checkIfEnabledFor(LogLevel logLevel) {
        if (!policy.isEnabled()) {
            return false;
        }

        return logLevel.value >= policy.getMinimumLevel().value;
    }
}
