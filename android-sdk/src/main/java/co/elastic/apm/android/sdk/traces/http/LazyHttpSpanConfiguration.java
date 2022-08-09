package co.elastic.apm.android.sdk.traces.http;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.utility.Provider;

public class LazyHttpSpanConfiguration implements Provider<HttpSpanConfiguration> {

    private HttpSpanConfiguration httpSpanConfiguration;

    @Override
    public HttpSpanConfiguration get() {
        if (httpSpanConfiguration == null) {
            httpSpanConfiguration = ElasticApmAgent.get().getHttpSpanConfiguration();
        }
        return httpSpanConfiguration;
    }
}
