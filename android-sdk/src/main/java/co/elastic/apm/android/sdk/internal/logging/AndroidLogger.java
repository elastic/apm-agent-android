/* 
Licensed to Elasticsearch B.V. under one or more contributor
license agreements. See the NOTICE file distributed with
this work for additional information regarding copyright
ownership. Elasticsearch B.V. licenses this file to you under
the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License. 
*/
package co.elastic.apm.android.sdk.internal.logging;

import android.util.Log;

import org.slf4j.event.Level;

class AndroidLogger extends BaseLogger {

    AndroidLogger(String tag) {
        super(tag);
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
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
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
