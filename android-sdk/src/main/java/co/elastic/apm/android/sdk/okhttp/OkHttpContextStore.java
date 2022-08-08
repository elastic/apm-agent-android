package co.elastic.apm.android.sdk.okhttp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.opentelemetry.context.Context;
import okhttp3.Request;

public class OkHttpContextStore {

    private final Map<Request, Context> spanContexts = Collections.synchronizedMap(new HashMap<>());

    public void put(Request request, Context spanContext) {
        spanContexts.put(request, spanContext);
    }

    public void remove(Request request) {
        spanContexts.remove(request);
    }

    public Context get(Request request) {
        return spanContexts.get(request);
    }
}
