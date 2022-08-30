package co.elastic.apm.android.sdk.traces.okhttp;

import java.io.IOException;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.attributes.AttributesCompose;
import co.elastic.apm.android.sdk.services.Service;
import co.elastic.apm.android.sdk.services.metadata.ApmMetadataService;
import co.elastic.apm.android.sdk.traces.http.HttpTraceConfiguration;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class OtelOkHttpEventListener extends EventListener {

    private static final String SPAN_NAME_FORMAT = "%s %s";
    private static final String OKHTTP_TRACER_NAME = "OkHttp";
    private final OkHttpContextStore contextStore;
    private HttpTraceConfiguration configuration;
    private Tracer okHttpTracer;

    public OtelOkHttpEventListener(OkHttpContextStore contextStore) {
        this.contextStore = contextStore;
    }

    @Override
    public void callStart(Call call) {
        super.callStart(call);
        Request request = call.request();
        String method = request.method();
        HttpUrl url = request.url();

        Context currentContext = Context.current();
        String host = url.host();
        AttributesCompose attributes = getConfiguration().createHttpAttributesCompose(convertRequest(request));
        Span span = getTracer().spanBuilder(String.format(SPAN_NAME_FORMAT, method, host))
                .setSpanKind(SpanKind.CLIENT)
                .setAllAttributes(attributes.provide())
                .setParent(currentContext)
                .startSpan();
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

    private HttpRequest convertRequest(Request request) {
        return new HttpRequest(request.method(), request.url().url());
    }

    private Tracer getTracer() {
        if (okHttpTracer == null) {
            ApmMetadataService service = ElasticApmAgent.get().getService(Service.Names.METADATA);
            String name = OKHTTP_TRACER_NAME;
            String version = service.getOkHttpVersion();
            if (version == null) {
                okHttpTracer = GlobalOpenTelemetry.getTracer(name);
            } else {
                okHttpTracer = GlobalOpenTelemetry.getTracer(name, version);
            }
        }

        return okHttpTracer;
    }

    private HttpTraceConfiguration getConfiguration() {
        if (configuration == null) {
            configuration = ElasticApmAgent.get().configuration.httpTraceConfiguration;
        }

        return configuration;
    }
}