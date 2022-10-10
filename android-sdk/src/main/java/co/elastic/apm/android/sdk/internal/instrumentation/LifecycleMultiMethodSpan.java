package co.elastic.apm.android.sdk.internal.instrumentation;

import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentMap;

import co.elastic.apm.android.sdk.internal.otel.SpanUtilities;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class LifecycleMultiMethodSpan {
    private static final WeakConcurrentMap<Span, Integer> methodCount = new WeakConcurrentMap.WithInlinedExpunction<>();

    public static SpanWithScope onMethodEnter(String ownerName, String methodName, ElasticTracer tracer) {
        ensureRootSpanIsCreated(ownerName, tracer);
        SpanBuilder spanBuilder = tracer.spanBuilder(methodName);
        Span span = spanBuilder.startSpan();
        Scope scope = span.makeCurrent();

        return new SpanWithScope(span, scope);
    }

    public static void onMethodExit(SpanWithScope spanWithScope, Throwable thrown, int maxMethods) {
        onMethodExit(spanWithScope, thrown, maxMethods, false);
    }

    public static void onMethodExit(SpanWithScope spanWithScope, Throwable thrown, int maxMethods, boolean forceEndRoot) {
        endMethodSpan(spanWithScope, thrown);

        Span rootSpan = Span.current();
        Integer endedSpans = methodCount.getIfPresent(rootSpan);
        endedSpans++;
        if (forceEndRoot || thrown != null || endedSpans == maxMethods) {
            endRootSpanAndCleanUp(rootSpan);
        } else {
            methodCount.put(rootSpan, endedSpans);
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
        methodCount.remove(rootSpan);
    }

    private static void ensureRootSpanIsCreated(String ownerName, ElasticTracer tracer) {
        if (SpanUtilities.runningSpanNotFound()) {
            SpanBuilder spanBuilder = tracer.spanBuilder(ownerName + " - View appearing");
            Span rootSpan = spanBuilder.startSpan();
            rootSpan.makeCurrent();
            methodCount.put(rootSpan, 0);
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
}
