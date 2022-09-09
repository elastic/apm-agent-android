package co.elastic.apm.android.instrumentation.okhttp.client;

import net.bytebuddy.asm.Advice;

import java.util.Collections;

import co.elastic.apm.android.sdk.traces.okhttp.OkHttpContextStore;
import co.elastic.apm.android.sdk.traces.okhttp.OtelOkHttpEventListener;
import co.elastic.apm.android.sdk.traces.okhttp.OtelOkHttpInterceptor;
import co.elastic.apm.android.sdk.traces.okhttp.compose.CompositeEventListenerFactory;
import okhttp3.OkHttpClient;

public class OkHttpClientAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Argument(0) OkHttpClient.Builder builder) {
        OkHttpContextStore contextStore = new OkHttpContextStore();
        OtelOkHttpEventListener.Factory otelFactory = new OtelOkHttpEventListener.Factory(contextStore);
        builder.eventListenerFactory(new CompositeEventListenerFactory(Collections.singletonList(otelFactory)));
        builder.addInterceptor(new OtelOkHttpInterceptor(contextStore));
    }
}