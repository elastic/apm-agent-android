package co.elastic.apm.android.sdk;

import co.elastic.apm.android.sdk.traces.http.HttpTraceConfiguration;

public final class ElasticApmConfiguration {
    public final HttpTraceConfiguration httpTraceConfiguration;
    public final String serviceName;
    public final String serviceVersion;

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
    }

    public static class Builder {
        private HttpTraceConfiguration httpTraceConfiguration;
        private String serviceName;
        private String serviceVersion;

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

        public ElasticApmConfiguration build() {
            if (httpTraceConfiguration == null) {
                httpTraceConfiguration = HttpTraceConfiguration.builder().build();
            }
            return new ElasticApmConfiguration(this);
        }
    }
}
