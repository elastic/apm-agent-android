package co.elastic.apm.android.sdk.okhttp;

import java.io.IOException;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.traces.http.HttpSpanConfiguration;
import co.elastic.apm.android.sdk.traces.http.LazyHttpSpanConfiguration;
import co.elastic.apm.android.sdk.traces.http.filtering.HttpSpanRule;
import co.elastic.apm.android.sdk.utility.Provider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class OtelOkHttpEventListener extends EventListener {

    private static final String SPAN_NAME_FORMAT = "%s %s";
    private final OkHttpContextStore contextStore;
    private final Provider<HttpSpanConfiguration> httpSpanConfigurationProvider;
    private HttpSpanRule httpSpanRule;

    public OtelOkHttpEventListener(OkHttpContextStore contextStore, LazyHttpSpanConfiguration ruleProvider) {
        this.contextStore = contextStore;
        this.httpSpanConfigurationProvider = ruleProvider;
    }

    @Override
    public void callStart(Call call) {
        super.callStart(call);
        Request request = call.request();
        String method = request.method();
        HttpUrl url = request.url();

        if (isNotSpannable(request, method, url)) {
            return;
        }

        Context currentContext = Context.current();
        String host = url.host();
        Span span = ElasticApmAgent.get().getTracer().spanBuilder(String.format(SPAN_NAME_FORMAT, method, host))
                .setSpanKind(SpanKind.CLIENT)
                .setParent(currentContext)
                .startSpan();
        span.setAttribute(SemanticAttributes.HTTP_URL, url.toString());
        span.setAttribute(SemanticAttributes.HTTP_METHOD, method);
        span.setAttribute(SemanticAttributes.HTTP_SCHEME, url.scheme());
        span.setAttribute(SemanticAttributes.HTTP_HOST, host);
        Context spanContext = currentContext.with(span);
        contextStore.put(request, spanContext);
    }

    @Override
    public void callEnd(Call call) {
        super.callEnd(call);
        Request request = call.request();
        Context context = getContext(request);
        if (context != null) {
            Span span = Span.fromContext(context);
            if (isValid(span)) {
                span.end();
            }
            contextStore.remove(request);
        }
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
        super.callFailed(call, ioe);
        Request request = call.request();
        Context context = getContext(request);
        if (context != null) {
            Span span = Span.fromContext(context);
            if (isValid(span)) {
                span.setStatus(StatusCode.ERROR);
                span.recordException(ioe);
                span.end();
            }
            contextStore.remove(request);
        }
    }

    private boolean isValid(Span span) {
        return span != null && span != Span.getInvalid();
    }

    Context getContext(Request request) {
        return contextStore.get(request);
    }

    private HttpSpanRule getHttpSpanRule() {
        if (httpSpanRule == null) {
            httpSpanRule = httpSpanConfigurationProvider.get().filterRule;
        }

        return httpSpanRule;
    }

    private boolean isNotSpannable(Request request, String method, HttpUrl url) {
        HttpSpanRule.HttpRequest data = new HttpSpanRule.HttpRequest(method, url.url(), request.headers().toMultimap());
        return !getHttpSpanRule().isSpannable(data);
    }
}