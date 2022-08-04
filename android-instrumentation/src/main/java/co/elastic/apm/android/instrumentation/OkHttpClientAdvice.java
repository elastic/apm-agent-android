package co.elastic.apm.android.instrumentation;

import net.bytebuddy.asm.Advice;

import okhttp3.OkHttpClient;

public class OkHttpClientAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Argument(0) OkHttpClient.Builder builder) {
    }
}