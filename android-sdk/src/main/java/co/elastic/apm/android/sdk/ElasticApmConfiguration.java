package co.elastic.apm.android.sdk;

import co.elastic.apm.android.sdk.traces.http.HttpSpanConfiguration;

public final class ElasticApmConfiguration {
    public final HttpSpanConfiguration httpSpanConfiguration;

    public static Builder builder() {
        return new Builder();
    }

    public static ElasticApmConfiguration getDefault() {
        return builder().build();
    }

    private ElasticApmConfiguration(Builder builder) {
        httpSpanConfiguration = builder.httpSpanConfiguration;
    }

    public static class Builder {
        private HttpSpanConfiguration httpSpanConfiguration;

        private Builder() {
        }

        public Builder setHttpSpanConfiguration(HttpSpanConfiguration httpSpanConfiguration) {
            this.httpSpanConfiguration = httpSpanConfiguration;
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
