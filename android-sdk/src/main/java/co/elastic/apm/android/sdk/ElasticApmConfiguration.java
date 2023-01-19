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

import co.elastic.apm.android.sdk.traces.http.HttpTraceConfiguration;
import co.elastic.apm.android.sdk.session.SessionIdProvider;
import co.elastic.apm.android.sdk.session.impl.DefaultSessionIdProvider;

public final class ElasticApmConfiguration {
    public final HttpTraceConfiguration httpTraceConfiguration;
    public final String serviceName;
    public final String serviceVersion;
    public final SessionIdProvider sessionIdProvider;

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
    }

    public static class Builder {
        private HttpTraceConfiguration httpTraceConfiguration;
        private String serviceName;
        private String serviceVersion;
        private SessionIdProvider sessionIdProvider;

        private Builder() {
        }

        public Builder setHttpTraceConfiguration(HttpTraceConfiguration httpTraceConfiguration) {
            this.httpTraceConfiguration = httpTraceConfiguration;
            return this;
        }

        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder setServiceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
            return this;
        }

        public Builder setSessionIdProvider(SessionIdProvider sessionIdProvider) {
            this.sessionIdProvider = sessionIdProvider;
            return this;
        }

        public ElasticApmConfiguration build() {
            if (httpTraceConfiguration == null) {
                httpTraceConfiguration = HttpTraceConfiguration.builder().build();
            }
            if (sessionIdProvider == null) {
                sessionIdProvider = new DefaultSessionIdProvider();
            }
            return new ElasticApmConfiguration(this);
        }
    }
}
