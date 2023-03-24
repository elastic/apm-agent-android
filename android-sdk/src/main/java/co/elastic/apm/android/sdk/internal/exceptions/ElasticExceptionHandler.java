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
package co.elastic.apm.android.sdk.internal.exceptions;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.logs.ElasticLoggers;
import io.opentelemetry.api.logs.EventBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public final class ElasticExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler wrapped;
    private static ElasticExceptionHandler INSTANCE;

    public static ElasticExceptionHandler getInstance() {
        if (INSTANCE == null) {
            Thread.UncaughtExceptionHandler existingHandler = Thread.getDefaultUncaughtExceptionHandler();
            if (existingHandler instanceof ElasticExceptionHandler) {
                // Needed for tests
                INSTANCE = (ElasticExceptionHandler) existingHandler;
            } else {
                INSTANCE = new ElasticExceptionHandler();
            }
        }
        return INSTANCE;
    }

    public static void resetForTest() {
        INSTANCE = null;
    }

    private ElasticExceptionHandler() {
        this.wrapped = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        emitCrashEvent(ElasticLoggers.crashReporter(), e);
        ElasticApmAgent.get().getFlusher().flushLogs().join(5, TimeUnit.SECONDS);

        if (wrapped != null) {
            wrapped.uncaughtException(t, e);
        }
    }

    private void emitCrashEvent(Logger crashReporter, @NonNull Throwable e) {
        EventBuilder crashEvent = crashReporter.eventBuilder("crash");
        crashEvent.setAttribute(SemanticAttributes.EXCEPTION_MESSAGE, e.getMessage());
        crashEvent.setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE, stackTraceToString(e));
        crashEvent.setAttribute(SemanticAttributes.EXCEPTION_TYPE, e.getClass().getName());
        crashEvent.emit();
    }

    private String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        throwable.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }
}
