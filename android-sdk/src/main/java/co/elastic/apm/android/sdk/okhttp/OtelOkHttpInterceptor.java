package co.elastic.apm.android.sdk.okhttp;

import java.io.IOException;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OtelOkHttpInterceptor implements Interceptor {

    private final OkHttpContextStore contextStore;

    public OtelOkHttpInterceptor(OkHttpContextStore contextStore) {
        this.contextStore = contextStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Context context = contextStore.get(request);
        if (context != null) {
            Request.Builder newRequestBuilder = request.newBuilder();
            TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
            propagator.inject(context, newRequestBuilder, new OtelOkhttpTextMapSetter());

            return chain.proceed(newRequestBuilder.build());
        }

        return chain.proceed(request);
    }

    static class OtelOkhttpTextMapSetter implements TextMapSetter<Request.Builder> {

        @Override
        public void set(Request.Builder carrier, String key, String value) {
            if (carrier == null) {
                return;
            }
            carrier.addHeader(key, value);
        }
    }
}