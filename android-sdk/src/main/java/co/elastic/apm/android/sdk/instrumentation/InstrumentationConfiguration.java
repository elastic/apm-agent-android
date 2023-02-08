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

import co.elastic.apm.android.sdk.instrumentation.supported.AppLaunchTimeConfiguration;
import co.elastic.apm.android.sdk.instrumentation.supported.CrashReportingConfiguration;
import co.elastic.apm.android.sdk.instrumentation.supported.HttpRequestsConfiguration;
import co.elastic.apm.android.sdk.instrumentation.supported.ScreenRenderingConfiguration;
import co.elastic.apm.android.sdk.internal.configuration.FeatureConfiguration;

public class InstrumentationConfiguration extends Instrumentation {
    private final HttpRequestsConfiguration httpRequestsConfiguration;
    private final ScreenRenderingConfiguration screenRenderingConfiguration;
    private final CrashReportingConfiguration crashReportingConfiguration;
    private final AppLaunchTimeConfiguration appLaunchTimeConfiguration;

    public InstrumentationConfiguration(HttpRequestsConfiguration httpRequestsConfiguration,
                                        ScreenRenderingConfiguration screenRenderingConfiguration,
                                        CrashReportingConfiguration crashReportingConfiguration,
                                        AppLaunchTimeConfiguration appLaunchTimeConfiguration) {
        this.httpRequestsConfiguration = httpRequestsConfiguration;
        this.screenRenderingConfiguration = screenRenderingConfiguration;
        this.crashReportingConfiguration = crashReportingConfiguration;
        this.appLaunchTimeConfiguration = appLaunchTimeConfiguration;
    }

    public HttpRequestsConfiguration getHttpRequestsConfiguration() {
        return httpRequestsConfiguration;
    }

    public ScreenRenderingConfiguration getScreenRenderingConfiguration() {
        return screenRenderingConfiguration;
    }

    public CrashReportingConfiguration getCrashReportingConfiguration() {
        return crashReportingConfiguration;
    }

    public AppLaunchTimeConfiguration getAppLaunchTimeConfiguration() {
        return appLaunchTimeConfiguration;
    }

    @Override
    protected Class<? extends FeatureConfiguration> getParentConfiguration() {
        return null;
    }

    @Override
    protected Type getInstrumentationType() {
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

        public InstrumentationConfiguration build() {
            return new InstrumentationConfiguration(
                    new HttpRequestsConfiguration(enableHttpRequests),
                    new ScreenRenderingConfiguration(enableScreenRendering),
                    new CrashReportingConfiguration(enableCrashReporting),
                    new AppLaunchTimeConfiguration(enableAppLaunchTime)
            );
        }
    }
}
