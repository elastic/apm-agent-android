package co.elastic.apm.android.sdk.traces.http.data;

import java.net.URL;

public class HttpRequest {
    public final String method;
    public final URL url;

    public HttpRequest(String method, URL url) {
        this.method = method;
        this.url = url;
    }
}
