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
package co.elastic.apm.android.sdk;

import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration;
import co.elastic.apm.android.sdk.session.SessionIdProvider;
import co.elastic.apm.android.sdk.session.impl.DefaultSessionIdProvider;
import co.elastic.apm.android.sdk.traces.http.HttpTraceConfiguration;
import co.elastic.apm.android.sdk.traces.tools.SpanFilter;

public final class ElasticApmConfiguration {
    public final HttpTraceConfiguration httpTraceConfiguration;
    public final InstrumentationConfiguration instrumentationConfiguration;
    public final String serviceName;
    public final String serviceVersion;
    public final String deploymentEnvironment;
    public final SessionIdProvider sessionIdProvider;
    public final SignalConfiguration signalConfiguration;
    public final SpanFilter spanFilter;

    public static Builder builder() {
        return new Builder();
    }

    public static ElasticApmConfiguration getDefault() {
        return builder().build();
    }

    private ElasticApmConfiguration(Builder builder) {
        httpTraceConfiguration = builder.httpTraceConfiguration;
        serviceName = builder.serviceName;
        serviceVersion = builder.serviceVersion;
        sessionIdProvider = builder.sessionIdProvider;
        instrumentationConfiguration = builder.instrumentationConfiguration;
        signalConfiguration = builder.signalConfiguration;
        deploymentEnvironment = builder.deploymentEnvironment;
        spanFilter = builder.spanFilter;
    }

    public static class Builder {
        private HttpTraceConfiguration httpTraceConfiguration;
        private InstrumentationConfiguration instrumentationConfiguration;
        private String serviceName;
        private String serviceVersion;
        private String deploymentEnvironment;
        private SessionIdProvider sessionIdProvider;
        private SignalConfiguration signalConfiguration;
        private SpanFilter spanFilter;

        private Builder() {
        }

        /**
         * Allows for configuring HTTP-related spans, such as, adding extra attributes and/or ignoring
         * some HTTP requests based on their structure.
         */
        public Builder setHttpTraceConfiguration(HttpTraceConfiguration httpTraceConfiguration) {
            this.httpTraceConfiguration = httpTraceConfiguration;
            return this;
        }

        /**
         * This method sets up OpenTelemetry's `service.name` resource attribute for every signal.
         * <p>
         * Despite its name, in this context it should refer to the application name.
         *
         * @param serviceName - The application name
         */
        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * This method sets up OpenTelemetry's `service.version` resource attribute for every signal.
         * <p>
         * Despite its name, in this context it should refer to the application version name.
         *
         * @param serviceVersion - The application version name
         */
        public Builder setServiceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
            return this;
        }

        /**
         * This method sets up OpenTelemetry's `deployment.environment` resource attribute for every signal.
         *
         * @param deploymentEnvironment - The application environment
         */
        public Builder setDeploymentEnvironment(String deploymentEnvironment) {
            this.deploymentEnvironment = deploymentEnvironment;
            return this;
        }

        /**
         * Used to enable/disable automatic instrumentations.
         */
        public Builder setInstrumentationConfiguration(InstrumentationConfiguration instrumentationConfiguration) {
            this.instrumentationConfiguration = instrumentationConfiguration;
            return this;
        }

        /**
         * This can be used to override OpenTelemetry's processors and exporters for all signals.
         */
        public Builder setSignalConfiguration(SignalConfiguration signalConfiguration) {
            this.signalConfiguration = signalConfiguration;
            return this;
        }

        /**
         * The span filter can be used to control which spans are exported and which shouldn't
         * leave the device. An implementation that always excludes all spans is essentially a way
         * to turn all spans off.
         */
        public Builder setSpanFilter(SpanFilter spanFilter) {
            this.spanFilter = spanFilter;
            return this;
        }

        public ElasticApmConfiguration build() {
            if (httpTraceConfiguration == null) {
                httpTraceConfiguration = HttpTraceConfiguration.builder().build();
            }
            if (instrumentationConfiguration == null) {
                instrumentationConfiguration = InstrumentationConfiguration.allEnabled();
            }
            if (sessionIdProvider == null) {
                sessionIdProvider = new DefaultSessionIdProvider();
            }
            return new ElasticApmConfiguration(this);
        }
    }
}
