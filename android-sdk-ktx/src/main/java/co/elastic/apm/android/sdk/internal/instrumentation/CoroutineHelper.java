package co.elastic.apm.android.sdk.internal.instrumentation;

import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import io.opentelemetry.api.trace.Span;

public class CoroutineHelper {

    public static Span startCoroutineSpan(String name) {
        return ElasticTracer.coroutine().spanBuilder(name).startSpan();
    }
}
