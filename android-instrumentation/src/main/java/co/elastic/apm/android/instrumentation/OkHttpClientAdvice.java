package co.elastic.apm.android.instrumentation;

import android.util.Log;

import net.bytebuddy.asm.Advice;

import co.elastic.apm.android.sdk.okhttp.OkhttpEventListener;
import okhttp3.OkHttpClient;

public class OkHttpClientAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Argument(0) OkHttpClient.Builder builder) {
        builder.eventListener(new OkhttpEventListener());
        Log.d("cesar", "Event listener is set");
    }
}