package co.elastic.apm.android.instrumentation.okhttp.eventlistener;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;

import co.elastic.apm.android.common.MethodCaller;

public class CompositeEventListenerAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.This MethodCaller owner, @Advice.Origin Method self, @Advice.AllArguments Object[] args) {
        owner.doCall(self, args);
    }
}
