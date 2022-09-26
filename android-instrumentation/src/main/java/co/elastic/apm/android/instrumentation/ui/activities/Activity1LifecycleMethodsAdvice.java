package co.elastic.apm.android.instrumentation.ui.activities;

import net.bytebuddy.asm.Advice;

import co.elastic.apm.android.sdk.internal.instrumentation.LifecycleMultiMethodSpan;

public class Activity1LifecycleMethodsAdvice {

    @Advice.OnMethodEnter
    public static void onMethodEnter(
            @Advice.This Object owner,
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope) {
        spanWithScope = LifecycleMultiMethodSpan.onMethodEnter(owner);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope,
            @Advice.Thrown Throwable thrown) {
        LifecycleMultiMethodSpan.onMethodExit(spanWithScope, thrown, 1);
    }
}
