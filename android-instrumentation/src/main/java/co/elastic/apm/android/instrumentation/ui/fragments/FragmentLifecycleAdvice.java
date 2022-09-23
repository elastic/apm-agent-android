package co.elastic.apm.android.instrumentation.ui.fragments;

import net.bytebuddy.asm.Advice;

import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;

public class FragmentLifecycleAdvice {

    @Advice.OnMethodEnter
    public static void onMethodEnter(@Advice.Local("elasticSpan") Span span,
                                     @Advice.Local("elasticScope") Scope scope) {
        SpanBuilder spanBuilder = ElasticTracer.androidFragment().spanBuilder();
        span = spanBuilder.startSpan();
        scope = span.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.Local("elasticSpan") Span span,
                                    @Advice.Local("elasticScope") Scope scope,
                                    @Advice.Thrown Throwable thrown) {
        if (thrown != null) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(thrown);
        }
        span.end();
        scope.close();
    }
}
