package co.elastic.apm.android.instrumentation.ui.activities;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;

import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class ActivityLifecycleAdvice {

    @Advice.OnMethodEnter
    public static void onMethodEnter(
            @Advice.This Object owner,
            @Advice.Local("elasticSpan") Span span,
            @Advice.Local("elasticScope") Scope scope) {
        if (Context.current() == Context.root()) {
            // Creating root span.
            SpanBuilder spanBuilder = ElasticTracer.androidActivity().spanBuilder(owner.getClass().getName() + " - Creating");
            Span rootSpan = spanBuilder.startSpan();
            rootSpan.makeCurrent();
        }
        // Creating method span.
        SpanBuilder spanBuilder = ElasticTracer.androidActivity().spanBuilder();
        span = spanBuilder.startSpan();
        scope = span.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(
            @Advice.Origin Method method,
            @Advice.Local("elasticSpan") Span span,
            @Advice.Local("elasticScope") Scope scope,
            @Advice.Thrown Throwable thrown) {
        if (thrown != null) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(thrown);
        }
        // Ending method span.
        span.end();
        scope.close();

        if (method.getName().equals("onResume")) {
            // Ending root span.
            Span rootSpan = Span.current();
            rootSpan.end();
            Context.root().makeCurrent();
        }
    }
}
