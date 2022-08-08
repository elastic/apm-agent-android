package co.elastic.apm.android.sdk.okhttp;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class OtelOkhttpEventListener extends EventListener {

    private static final String SPAN_NAME_FORMAT = "%s %s";
    private final Map<Request, Context> spanContexts = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void callStart(Call call) {
        super.callStart(call);
        Context currentContext = Context.current();
        Request request = call.request();
        String method = request.method();
        HttpUrl url = request.url();
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
        spanContexts.put(request, spanContext);
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
            spanContexts.remove(request);
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
            spanContexts.remove(request);
        }
    }

    private boolean isValid(Span span) {
        return span != null && span != Span.getInvalid();
    }

    private Context getContext(Request request) {
        return spanContexts.get(request);
    }
}