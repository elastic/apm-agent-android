package co.elastic.apm.android.sdk.traces.http.filtering;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HttpSpanRule {

    boolean isSpannable(HttpRequest request);

    static HttpSpanRule composite(Set<HttpSpanRule> rules) {
        return MultiHttpSpanRule.create(rules);
    }

    static HttpSpanRule allowAll() {
        return SpanAllRule.INSTANCE;
    }

    class HttpRequest {
        final String method;
        final URL url;
        final Map<String, List<String>> headers;

        public HttpRequest(String method, URL url, Map<String, List<String>> headers) {
            this.method = method;
            this.url = url;
            this.headers = headers;
        }
    }
}
