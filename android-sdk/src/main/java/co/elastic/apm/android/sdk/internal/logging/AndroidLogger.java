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
package co.elastic.apm.android.sdk.internal.logging;

import android.util.Log;

import org.slf4j.event.Level;

import co.elastic.apm.android.common.internal.logging.BaseELogger;
import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.appinfo.AppInfoService;
import co.elastic.apm.android.sdk.internal.providers.LazyProvider;

class AndroidLogger extends BaseELogger {
    private final LazyProvider<Boolean> appIsDebuggable;

    AndroidLogger(String tag) {
        super(tag);
        appIsDebuggable = LazyProvider.of(() -> {
            AppInfoService service = ElasticApmAgent.get().getService(Service.Names.APP_INFO);
            return service.isInDebugMode();
        });
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
        return appIsDebuggable.get();
    }

    @Override
    public boolean isDebugEnabled() {
        return appIsDebuggable.get();
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }
}
