package co.elastic.apm.android.instrumentation;

import net.bytebuddy.asm.Advice;

import co.elastic.apm.android.sdk.traces.okhttp.OkHttpContextStore;
import co.elastic.apm.android.sdk.traces.okhttp.OtelOkHttpEventListener;
import co.elastic.apm.android.sdk.traces.okhttp.OtelOkHttpInterceptor;
import okhttp3.OkHttpClient;

public class OkHttpClientAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Argument(0) OkHttpClient.Builder builder) {
        OkHttpContextStore contextStore = new OkHttpContextStore();
        builder.eventListener(new OtelOkHttpEventListener(contextStore));
        builder.addInterceptor(new OtelOkHttpInterceptor(contextStore));
    }
}