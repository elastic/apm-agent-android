package co.elastic.apm.android.instrumentation.ui.fragments;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;

import co.elastic.apm.android.sdk.internal.instrumentation.LifecycleMultiMethodSpan;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;

public class Fragment3LifecycleMethodAdvice {

    @Advice.OnMethodEnter
    public static void onMethodEnter(
            @Advice.Origin("#t") String ownerName,
            @Advice.Origin("#m") String methodName,
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope) {
        spanWithScope = LifecycleMultiMethodSpan.onMethodEnter(ownerName, methodName, ElasticTracer.androidFragment());
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(
            @Advice.Origin Method method,
            @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returned,
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope,
            @Advice.Thrown Throwable thrown) {
        boolean forceEndRoot = false;
        if (!method.getReturnType().equals(void.class)) {
            forceEndRoot = returned == null;
        }
        LifecycleMultiMethodSpan.onMethodExit(spanWithScope, thrown, 3, forceEndRoot);
    }
}
