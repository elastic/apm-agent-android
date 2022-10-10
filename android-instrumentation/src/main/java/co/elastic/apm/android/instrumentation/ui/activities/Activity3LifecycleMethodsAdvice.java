package co.elastic.apm.android.instrumentation.ui.activities;

import net.bytebuddy.asm.Advice;

import co.elastic.apm.android.sdk.internal.instrumentation.LifecycleMultiMethodSpan;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;

public class Activity3LifecycleMethodsAdvice {

    @Advice.OnMethodEnter
    public static void onMethodEnter(
            @Advice.Origin("#t") String ownerName,
            @Advice.Origin("#m") String methodName,
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope) {
        spanWithScope = LifecycleMultiMethodSpan.onMethodEnter(ownerName, methodName, ElasticTracer.androidActivity());
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope,
            @Advice.Thrown Throwable thrown) {
        LifecycleMultiMethodSpan.onMethodExit(spanWithScope, thrown, 3);
    }
}
