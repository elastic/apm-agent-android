package co.elastic.apm.android.sdk;

import android.content.Context;

import co.elastic.apm.android.sdk.traces.http.HttpSpanConfiguration;

public final class ElasticApmConfiguration {
    public final HttpSpanConfiguration httpSpanConfiguration;

    public static Builder builder(Context appContext) {
        return new Builder(appContext);
    }

    public static ElasticApmConfiguration getDefault(Context context) {
        return builder(context).build();
    }

    private ElasticApmConfiguration(Builder builder) {
        httpSpanConfiguration = builder.httpSpanConfiguration;
    }

    public static class Builder {
        private HttpSpanConfiguration httpSpanConfiguration;

        private Builder(Context context) {
            Context appContext = context.getApplicationContext();
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
