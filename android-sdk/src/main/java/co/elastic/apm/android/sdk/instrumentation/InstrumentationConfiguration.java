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

import co.elastic.apm.android.sdk.instrumentation.supported.AppLaunchTimeInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.CrashReportingInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.HttpRequestsInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.ScreenRenderingInstrumentation;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.instrumentation.GroupInstrumentation;

public final class InstrumentationConfiguration extends GroupInstrumentation {
    private final HttpRequestsInstrumentation httpRequestsConfiguration;
    private final ScreenRenderingInstrumentation screenRenderingConfiguration;
    private final CrashReportingInstrumentation crashReportingConfiguration;
    private final AppLaunchTimeInstrumentation appLaunchTimeConfiguration;

    public static InstrumentationConfiguration.Builder builder() {
        return new Builder();
    }

    public static InstrumentationConfiguration allEnabled() {
        return builder()
                .enableCrashReporting(true)
                .enableHttpRequests(true)
                .enableAppLaunchTime(true)
                .enableScreenRendering(true)
                .build();
    }

    public static InstrumentationConfiguration allDisabled() {
        return builder().build();
    }

    public InstrumentationConfiguration(
            boolean enabled,
            HttpRequestsInstrumentation httpRequestsConfiguration,
            ScreenRenderingInstrumentation screenRenderingConfiguration,
            CrashReportingInstrumentation crashReportingConfiguration,
            AppLaunchTimeInstrumentation appLaunchTimeConfiguration) {
        super(enabled);
        this.httpRequestsConfiguration = httpRequestsConfiguration;
        this.screenRenderingConfiguration = screenRenderingConfiguration;
        this.crashReportingConfiguration = crashReportingConfiguration;
        this.appLaunchTimeConfiguration = appLaunchTimeConfiguration;
    }

    public HttpRequestsInstrumentation getHttpRequestsConfiguration() {
        return httpRequestsConfiguration;
    }

    public ScreenRenderingInstrumentation getScreenRenderingConfiguration() {
        return screenRenderingConfiguration;
    }

    public CrashReportingInstrumentation getCrashReportingConfiguration() {
        return crashReportingConfiguration;
    }

    public AppLaunchTimeInstrumentation getAppLaunchTimeConfiguration() {
        return appLaunchTimeConfiguration;
    }

    @Override
    protected Class<? extends Configuration> getParentConfigurationType() {
        return null;
    }

    @Override
    protected boolean enabled() {
        return true;
    }

    public static class Builder {
        private boolean enableHttpRequests;
        private boolean enableScreenRendering;
        private boolean enableCrashReporting;
        private boolean enableAppLaunchTime;

        private Builder() {
        }

        public Builder enableHttpRequests(boolean enableHttpRequests) {
            this.enableHttpRequests = enableHttpRequests;
            return this;
        }

        public Builder enableScreenRendering(boolean enableScreenRendering) {
            this.enableScreenRendering = enableScreenRendering;
            return this;
        }

        public Builder enableCrashReporting(boolean enableCrashReporting) {
            this.enableCrashReporting = enableCrashReporting;
            return this;
        }

        public Builder enableAppLaunchTime(boolean enableAppLaunchTime) {
            this.enableAppLaunchTime = enableAppLaunchTime;
            return this;
        }

        public InstrumentationConfiguration build() {
            return new InstrumentationConfiguration(
                    true,
                    new HttpRequestsInstrumentation(enableHttpRequests),
                    new ScreenRenderingInstrumentation(enableScreenRendering),
                    new CrashReportingInstrumentation(enableCrashReporting),
                    new AppLaunchTimeInstrumentation(enableAppLaunchTime)
            );
        }
    }
}
