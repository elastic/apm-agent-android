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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.features.persistence.PersistenceConfiguration;
import co.elastic.apm.android.sdk.instrumentation.InstrumentationConfiguration;
import co.elastic.apm.android.sdk.logs.tools.LogFilter;
import co.elastic.apm.android.sdk.metrics.tools.MetricFilter;
import co.elastic.apm.android.sdk.session.SessionIdGenerator;
import co.elastic.apm.android.sdk.session.impl.DefaultSessionIdGenerator;
import co.elastic.apm.android.sdk.traces.http.HttpTraceConfiguration;
import co.elastic.apm.android.sdk.traces.tools.SpanFilter;

public final class ElasticApmConfiguration {
    public final HttpTraceConfiguration httpTraceConfiguration;
    public final InstrumentationConfiguration instrumentationConfiguration;
    public final String serviceName;
    public final String serviceVersion;
    public final String deploymentEnvironment;
    public final SessionIdGenerator sessionIdGenerator;
    public final SignalConfiguration signalConfiguration;
    public final PersistenceConfiguration persistenceConfiguration;
    public final List<SpanFilter> spanFilters;
    public final List<LogFilter> logFilters;
    public final List<MetricFilter> metricFilters;

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
        sessionIdGenerator = builder.sessionIdGenerator;
        instrumentationConfiguration = builder.instrumentationConfiguration;
        signalConfiguration = builder.signalConfiguration;
        deploymentEnvironment = builder.deploymentEnvironment;
        persistenceConfiguration = builder.persistenceConfiguration;
        spanFilters = Collections.unmodifiableList(new ArrayList<>(builder.spanFilters));
        logFilters = Collections.unmodifiableList(new ArrayList<>(builder.logFilters));
        metricFilters = Collections.unmodifiableList(new ArrayList<>(builder.metricFilters));
    }

    public static class Builder {
        private HttpTraceConfiguration httpTraceConfiguration;
        private InstrumentationConfiguration instrumentationConfiguration;
        private PersistenceConfiguration persistenceConfiguration;
        private String serviceName;
        private String serviceVersion;
        private String deploymentEnvironment;
        private SessionIdGenerator sessionIdGenerator;
        private SignalConfiguration signalConfiguration;
        private final Set<SpanFilter> spanFilters = new HashSet<>();
        private final Set<LogFilter> logFilters = new HashSet<>();
        private final Set<MetricFilter> metricFilters = new HashSet<>();

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
         * Sets the parameters for caching signals in disk in order to export them later.
         */
        public Builder setPersistenceConfiguration(PersistenceConfiguration persistenceConfiguration) {
            this.persistenceConfiguration = persistenceConfiguration;
            return this;
        }

        /**
         * The session ID generator will be used when a new session is created, the id provided by this generator will be the session ID.
         * By default, a UUID is generated.
         */
        public void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {
            this.sessionIdGenerator = sessionIdGenerator;
        }

        /**
         * The span filter can be used to control which spans are exported and which shouldn't
         * leave the device. An implementation that always excludes all spans is essentially a way
         * to turn all spans off.
         */
        public Builder addSpanFilter(SpanFilter spanFilter) {
            spanFilters.add(spanFilter);
            return this;
        }

        /**
         * The log filter can be used to control which log records are exported and which shouldn't
         * leave the device. An implementation that always excludes all logs is essentially a way
         * to turn all log records off.
         */
        public Builder addLogFilter(LogFilter logFilter) {
            logFilters.add(logFilter);
            return this;
        }

        /**
         * The metric filter can be used to control which metrics are exported and which shouldn't
         * leave the device. An implementation that always excludes all metrics is essentially a way
         * to turn all metrics off.
         */
        public Builder addMetricFilter(MetricFilter metricFilter) {
            metricFilters.add(metricFilter);
            return this;
        }

        public ElasticApmConfiguration build() {
            if (httpTraceConfiguration == null) {
                httpTraceConfiguration = HttpTraceConfiguration.builder().build();
            }
            if (instrumentationConfiguration == null) {
                instrumentationConfiguration = InstrumentationConfiguration.allEnabled();
            }
            if (sessionIdGenerator == null) {
                sessionIdGenerator = new DefaultSessionIdGenerator();
            }
            if (persistenceConfiguration == null) {
                persistenceConfiguration = PersistenceConfiguration.builder().build();
            }
            return new ElasticApmConfiguration(this);
        }
    }
}
