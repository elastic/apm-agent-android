package co.elastic.apm.android.sdk;

import android.content.Context;

import co.elastic.apm.android.sdk.attributes.AttributesCompose;
import co.elastic.apm.android.sdk.services.ServiceManager;
import co.elastic.apm.android.sdk.services.network.NetworkService;
import co.elastic.apm.android.sdk.services.permissions.AndroidPermissionService;
import co.elastic.apm.android.sdk.traces.http.HttpSpanConfiguration;

public final class ElasticApmConfiguration {
    public final HttpSpanConfiguration httpSpanConfiguration;
    final AttributesCompose globalAttributes;
    final ServiceManager serviceManager;

    public static Builder builder(Context appContext) {
        return new Builder(appContext);
    }

    public static ElasticApmConfiguration getDefault(Context context) {
        return builder(context).build();
    }

    private ElasticApmConfiguration(Builder builder) {
        httpSpanConfiguration = builder.httpSpanConfiguration;
        globalAttributes = builder.globalAttributes;
        serviceManager = builder.serviceManager;
    }

    public static class Builder {
        private final AttributesCompose globalAttributes;
        private final ServiceManager serviceManager;
        private HttpSpanConfiguration httpSpanConfiguration;

        private Builder(Context context) {
            Context appContext = context.getApplicationContext();
            globalAttributes = AttributesCompose.global(appContext);
            serviceManager = new ServiceManager();
            serviceManager.addService(new NetworkService(appContext));
            serviceManager.addService(new AndroidPermissionService(appContext));
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
