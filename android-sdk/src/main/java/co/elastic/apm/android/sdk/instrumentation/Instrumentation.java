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
package co.elastic.apm.android.sdk.instrumentation;

import androidx.annotation.NonNull;

import co.elastic.apm.android.sdk.instrumentation.supported.AppLaunchTimeInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.CrashReportingInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.HttpRequestsInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.ScreenRenderingInstrumentation;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.instrumentation.SupportedInstrumentation;

public abstract class Instrumentation extends Configuration {

    public static <T extends Instrumentation> void runWhenEnabled(Class<T> type, Function<T> onEnabled) {
        if (Configurations.isEnabled(type)) {
            onEnabled.onInstrumentationReady(Configurations.get(type));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends SupportedInstrumentation> void runWhenEnabled(Supported instrumentation, Function<T> onEnabled) {
        runWhenEnabled((Class<T>) instrumentation.type, onEnabled);
    }

    public static void runHttpRequestsWhenEnabled(Function<HttpRequestsInstrumentation> function) {
        runWhenEnabled(Supported.HTTP_REQUESTS, function);
    }

    @NonNull
    protected Group getGroup() {
        return Group.NONE;
    }

    @Override
    protected Class<? extends Configuration> getParentConfigurationType() {
        return getGroup().instrumentation.type;
    }

    public enum Group {
        NONE(Supported.GENERAL);

        private final Supported instrumentation;

        Group(Supported instrumentation) {
            this.instrumentation = instrumentation;
        }
    }

    public enum Supported {
        GENERAL(InstrumentationConfiguration.class),
        HTTP_REQUESTS(HttpRequestsInstrumentation.class),
        APP_LAUNCH_TIME(AppLaunchTimeInstrumentation.class),
        SCREEN_RENDERING(ScreenRenderingInstrumentation.class),
        CRASH_REPORTING(CrashReportingInstrumentation.class);

        private final Class<? extends SupportedInstrumentation> type;

        Supported(Class<? extends SupportedInstrumentation> type) {
            this.type = type;
        }
    }

    @FunctionalInterface
    public interface Function<T extends Instrumentation> {
        void onInstrumentationReady(T instrumentation);
    }
}
