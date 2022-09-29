package co.elastic.apm.android.instrumentation.coroutines.flow;

import net.bytebuddy.asm.Advice;

import co.elastic.apm.android.sdk.internal.otel.SpanUtilities;

public class CoroutineFlowBlockersAdvice {

    @Advice.OnMethodEnter
    public static void onMethodEnter() {
        SpanUtilities.tryRevertCurrentContext();
    }
}
