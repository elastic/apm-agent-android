package co.elastic.apm.android.sdk;

import co.elastic.apm.android.sdk.traces.http.HttpSpanConfiguration;

public final class ElasticApmConfiguration {
    public final HttpSpanConfiguration httpSpanConfiguration;
    public final String serviceName;
    public final String serviceVersion;

    public static Builder builder() {
        return new Builder();
    }

    public static ElasticApmConfiguration getDefault() {
        return builder().build();
    }

    private ElasticApmConfiguration(Builder builder) {
        httpSpanConfiguration = builder.httpSpanConfiguration;
        serviceName = builder.serviceName;
        serviceVersion = builder.serviceVersion;
    }

    public static class Builder {
        private HttpSpanConfiguration httpSpanConfiguration;
        private String serviceName;
        private String serviceVersion;

        private Builder() {
        }

        public Builder setHttpSpanConfiguration(HttpSpanConfiguration httpSpanConfiguration) {
            this.httpSpanConfiguration = httpSpanConfiguration;
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
            if (httpSpanConfiguration == null) {
                httpSpanConfiguration = HttpSpanConfiguration.builder().build();
            }
            return new ElasticApmConfiguration(this);
        }
    }
}
