package co.elastic.apm.android.sdk.internal.instrumentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import co.elastic.apm.android.sdk.internal.otel.SpanUtilities;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class LifecycleMultiMethodSpan {

    public static SpanWithScope onMethodEnter(String ownerName, String methodName, ElasticTracer tracer) {
        ensureRootSpanIsCreated(ownerName, tracer);
        SpanBuilder spanBuilder = tracer.spanBuilder(methodName);
        Span span = spanBuilder.startSpan();
        Scope scope = span.makeCurrent();

        return new SpanWithScope(span, scope);
    }

    public static void onMethodExit(SpanWithScope spanWithScope, Throwable thrown, boolean endRoot) {
        endMethodSpan(spanWithScope, thrown);

        Span rootSpan = Span.current();
        if (endRoot || thrown != null) {
            endRootSpanAndCleanUp(rootSpan);
        }
    }

    private static void endMethodSpan(SpanWithScope spanWithScope, Throwable thrown) {
        Span span = spanWithScope.span;
        Scope scope = spanWithScope.scope;
        if (thrown != null) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(thrown);
        }
        span.end();
        scope.close();
    }

    private static void endRootSpanAndCleanUp(Span rootSpan) {
        rootSpan.end();
        Context.root().makeCurrent();
    }

    private static void ensureRootSpanIsCreated(String ownerName, ElasticTracer tracer) {
        if (SpanUtilities.runningSpanNotFound()) {
            SpanBuilder spanBuilder = tracer.spanBuilder(ownerName + " - View appearing");
            Span rootSpan = spanBuilder.startSpan();
            rootSpan.makeCurrent();
        }
    }

    public static class SpanWithScope {
        public final Span span;
        public final Scope scope;

        public SpanWithScope(Span span, Scope scope) {
            this.span = span;
            this.scope = scope;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface LastMethod {

    }
}
