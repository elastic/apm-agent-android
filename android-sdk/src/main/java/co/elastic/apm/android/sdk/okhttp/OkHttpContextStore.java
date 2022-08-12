package co.elastic.apm.android.sdk.okhttp;

import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentMap;

import io.opentelemetry.context.Context;
import okhttp3.Request;

public class OkHttpContextStore {

    private final WeakConcurrentMap<Request, Context> spanContexts = new WeakConcurrentMap.WithInlinedExpunction<>();

    public void put(Request request, Context spanContext) {
        spanContexts.put(request, spanContext);
    }

    public void remove(Request request) {
        spanContexts.remove(request);
    }

    public Context get(Request request) {
        return spanContexts.getIfPresent(request);
    }
}
