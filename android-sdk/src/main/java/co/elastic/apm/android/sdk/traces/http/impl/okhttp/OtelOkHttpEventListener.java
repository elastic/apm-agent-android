/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.sdk.traces.http.impl.okhttp;

import androidx.annotation.NonNull;

import java.io.IOException;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.attributes.AttributesCreator;
import co.elastic.apm.android.sdk.attributes.AttributesVisitor;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import co.elastic.apm.android.sdk.traces.http.HttpTraceConfiguration;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import co.elastic.apm.android.sdk.traces.http.impl.okhttp.utils.WrapperSpanCloser;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class OtelOkHttpEventListener extends EventListener {

    private static final String SPAN_NAME_FORMAT = "%s %s";
    private static final String TRANSACTION_SPAN_NAME_FORMAT = "Transaction - " + SPAN_NAME_FORMAT;
    private static final ContextKey<WrapperSpanCloser> WRAPPER_CLOSER_KEY = ContextKey.named("wrapper-closer");
    private final OkHttpContextStore contextStore;
    private HttpTraceConfiguration configuration;
    private Tracer okHttpTracer;

    private OtelOkHttpEventListener(OkHttpContextStore contextStore) {
        this.contextStore = contextStore;
    }

    @Override
    public void callStart(Call call) {
        super.callStart(call);
        Request request = call.request();
        Elog.getLogger().info("Intercepting OkHttp request");
        Elog.getLogger().debug("Intercepting OkHttp request: {}", request.url());
        String method = request.method();
        HttpUrl url = request.url();
        String host = url.host();
        Tracer okhttpTracer = getTracer();

        Context parentContext = Context.current();
        Span wrapperSpan = null;
        if (thereIsNoParentSpan(parentContext)) {
            wrapperSpan = createWrapperSpan(okhttpTracer, method, host);
            parentContext = parentContext.with(wrapperSpan);
        }

        AttributesVisitor httpAttributes = getConfiguration().createHttpAttributesVisitor(convertRequest(request));
        Span span = okhttpTracer.spanBuilder(String.format(SPAN_NAME_FORMAT, method, host))
                .setSpanKind(SpanKind.CLIENT)
                .setAllAttributes(AttributesCreator.from(httpAttributes).create())
                .setParent(parentContext)
                .startSpan();
        Context spanContext = parentContext.with(span);

        if (wrapperSpan != null) {
            // Attaching the wrapper span to end it right after the http span is ended.
            spanContext = spanContext.with(WRAPPER_CLOSER_KEY, wrapperSpan::end);
        }

        contextStore.put(request, spanContext);
    }

    private Span createWrapperSpan(Tracer okhttpTracer, String method, String host) {
        Elog.getLogger().info("Creating wrapper span");
        return okhttpTracer.spanBuilder(String.format(TRANSACTION_SPAN_NAME_FORMAT, method, host))
                .startSpan();
    }

    private boolean thereIsNoParentSpan(Context parentContext) {
        return parentContext == Context.root();
    }

    @Override
    public void callEnd(Call call) {
        super.callEnd(call);
        Request request = call.request();
        Elog.getLogger().info("OkHttp request ended");
        Elog.getLogger().debug("OkHttp request ended: {}", request.url());
        Context context = getContext(request);
        if (context != null) {
            Span span = Span.fromContext(context);
            if (isValid(span)) {
                endSpan(span, context);
            }
            contextStore.remove(request);
        }
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
        super.callFailed(call, ioe);
        Request request = call.request();
        Elog.getLogger().info("OkHttp request failed");
        Elog.getLogger().debug("OkHttp request failed: {}", request.url());
        Context context = getContext(request);
        if (context != null) {
            Span span = Span.fromContext(context);
            if (isValid(span)) {
                span.setStatus(StatusCode.ERROR);
                span.recordException(ioe);
                endSpan(span, context);
            }
            contextStore.remove(request);
        }
    }

    private void endSpan(Span span, Context context) {
        span.end();
        endWrapperIfAny(context);
    }

    private void endWrapperIfAny(Context context) {
        WrapperSpanCloser wrapperSpanCloser = context.get(WRAPPER_CLOSER_KEY);
        if (wrapperSpanCloser != null) {
            Elog.getLogger().info("Closing wrapper span");
            wrapperSpanCloser.closeWrapper();
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
            okHttpTracer = ElasticTracer.okhttp();
        }

        return okHttpTracer;
    }

    private HttpTraceConfiguration getConfiguration() {
        if (configuration == null) {
            configuration = ElasticApmAgent.get().configuration.httpTraceConfiguration;
        }

        return configuration;
    }

    public static class Factory implements EventListener.Factory {
        private final OkHttpContextStore contextStore;

        public Factory(OkHttpContextStore contextStore) {
            this.contextStore = contextStore;
        }

        @NonNull
        @Override
        public EventListener create(@NonNull Call call) {
            return new OtelOkHttpEventListener(contextStore);
        }
    }
}