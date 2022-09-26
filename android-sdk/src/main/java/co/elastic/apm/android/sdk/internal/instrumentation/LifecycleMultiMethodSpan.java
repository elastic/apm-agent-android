package co.elastic.apm.android.sdk.internal.instrumentation;

import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentMap;

import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class LifecycleMultiMethodSpan {
    private static final WeakConcurrentMap<Span, Integer> methodCount = new WeakConcurrentMap.WithInlinedExpunction<>();

    public static SpanWithScope onMethodEnter(Object owner) {
        ensureRootSpanIsCreated(owner);
        SpanBuilder spanBuilder = ElasticTracer.androidActivity().spanBuilder();
        Span span = spanBuilder.startSpan();
        Scope scope = span.makeCurrent();

        return new SpanWithScope(span, scope);
    }

    public static void onMethodExit(SpanWithScope spanWithScope, Throwable thrown, int maxMethods) {
        endMethodSpan(spanWithScope, thrown);

        Span rootSpan = Span.current();
        Integer endedSpans = methodCount.getIfPresent(rootSpan);
        endedSpans++;
        if (endedSpans == maxMethods) {
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

    private static void ensureRootSpanIsCreated(Object owner) {
        if (Context.current() == Context.root()) {
            SpanBuilder spanBuilder = ElasticTracer.androidActivity().spanBuilder(owner.getClass().getName() + " - Creating");
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
