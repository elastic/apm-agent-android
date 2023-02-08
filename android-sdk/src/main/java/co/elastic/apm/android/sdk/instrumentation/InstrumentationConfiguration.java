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

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.sdk.instrumentation.supported.AppLaunchTimeInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.CrashReportingInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.HttpRequestsInstrumentation;
import co.elastic.apm.android.sdk.instrumentation.supported.ScreenRenderingInstrumentation;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.instrumentation.GroupInstrumentation;

public final class InstrumentationConfiguration extends GroupInstrumentation {
    public final List<Instrumentation> instrumentations;

    public static InstrumentationConfiguration.Builder builder() {
        return new Builder(true);
    }

    public static InstrumentationConfiguration allEnabled() {
        return builder().build();
    }

    public static InstrumentationConfiguration allDisabled() {
        return new Builder(false).build();
    }

    public InstrumentationConfiguration(
            boolean enabled,
            List<Instrumentation> instrumentations) {
        super(enabled);
        this.instrumentations = instrumentations;
    }

    @Override
    protected Class<? extends Configuration> getParentConfigurationType() {
        return null;
    }

    public static class Builder {
        private final boolean enabled;
        private boolean enableHttpRequests;
        private boolean enableScreenRendering;
        private boolean enableCrashReporting;
        private boolean enableAppLaunchTimeMetric;

        private Builder(boolean enabled) {
            this.enabled = enabled;
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

        public Builder enableAppLaunchTimeMetric(boolean enableAppLaunchTime) {
            this.enableAppLaunchTimeMetric = enableAppLaunchTime;
            return this;
        }

        public InstrumentationConfiguration build() {
            List<Instrumentation> instrumentations = new ArrayList<>();
            instrumentations.add(new HttpRequestsInstrumentation(enableHttpRequests));
            instrumentations.add(new ScreenRenderingInstrumentation(enableScreenRendering));
            instrumentations.add(new CrashReportingInstrumentation(enableCrashReporting));
            instrumentations.add(new AppLaunchTimeInstrumentation(enableAppLaunchTimeMetric));
            return new InstrumentationConfiguration(enabled, instrumentations);
        }
    }
}
