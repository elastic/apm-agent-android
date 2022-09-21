package co.elastic.apm.android.instrumentation.ui.lifecycle;

import net.bytebuddy.asm.Advice;

public class ActivityLifecycleAdvice {

    @Advice.OnMethodEnter
    public static void onCreateAdvice(@Advice.This Object owner) {

    }
}
