package co.elastic.apm.android.instrumentation;

import net.bytebuddy.asm.Advice;

import co.elastic.apm.android.sdk.okhttp.OkhttpContextStore;
import co.elastic.apm.android.sdk.okhttp.OtelOkhttpEventListener;
import co.elastic.apm.android.sdk.okhttp.OtelOkhttpInterceptor;
import okhttp3.OkHttpClient;

public class OkHttpClientAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Argument(0) OkHttpClient.Builder builder) {
        OkhttpContextStore contextStore = new OkhttpContextStore();
        builder.eventListener(new OtelOkhttpEventListener(contextStore));
        builder.addInterceptor(new OtelOkhttpInterceptor(contextStore));
    }
}