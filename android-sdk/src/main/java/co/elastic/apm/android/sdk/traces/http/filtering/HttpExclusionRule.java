package co.elastic.apm.android.sdk.traces.http.filtering;

import java.net.MalformedURLException;
import java.net.URL;

import co.elastic.apm.android.sdk.traces.otel.sampler.ExclusiveSampler;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

abstract public class HttpExclusionRule implements ExclusiveSampler.Rule {

    @Override
    public boolean exclude(String spanName, SpanKind kind, Attributes attributes) {
        String httpMethod = attributes.get(SemanticAttributes.HTTP_METHOD);
        if (httpMethod == null) {
            // Not an http-related Span.
            return false;
        }

        return exclude(new Request(httpMethod, getUrl(attributes)));
    }

    private URL getUrl(Attributes attributes) {
        String urlString = attributes.get(SemanticAttributes.HTTP_URL);
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    abstract boolean exclude(Request request);

    static class Request {
        public final String method;
        public final URL url;

        public Request(String method, URL url) {
            this.method = method;
            this.url = url;
        }
    }
}