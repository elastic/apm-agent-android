package co.elastic.apm.android.instrumentation.ui.fragments;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;

import co.elastic.apm.android.sdk.internal.instrumentation.LifecycleMultiMethodSpan;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;

public class FragmentLifecycleMethodAdvice {

    @Advice.OnMethodEnter
    public static void onMethodEnter(
            @Advice.Origin("#t") String ownerName,
            @Advice.Origin("#m") String methodName,
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope) {
        spanWithScope = LifecycleMultiMethodSpan.onMethodEnter(ownerName, methodName, ElasticTracer.androidFragment());
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(
            @Advice.This Object owner,
            @Advice.Origin Method method,
            @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returned,
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope,
            @Advice.Thrown Throwable thrown) {
        boolean endRoot = false;
        if (!method.getReturnType().equals(void.class)) {
            endRoot = returned == null;
        }
        LifecycleMultiMethodSpan.onMethodExit(owner, spanWithScope, thrown, endRoot || method.isAnnotationPresent(LifecycleMultiMethodSpan.LastMethod.class));
    }
}
