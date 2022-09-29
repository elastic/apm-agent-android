package co.elastic.apm.android.sdk.traces.common.tools;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;

public class ElasticTracer implements Tracer {
    private final Tracer tracer;

    public ElasticTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public static ElasticTracer create(@NonNull String name, @Nullable String version) {
        if (version == null) {
            return new ElasticTracer(GlobalOpenTelemetry.getTracer(name));
        } else {
            return new ElasticTracer(GlobalOpenTelemetry.getTracer(name, version));
        }
    }

    public static ElasticTracer create(String name) {
        return create(name, null);
    }

    public static ElasticTracer okhttp() {
        ApmMetadataService service = ElasticApmAgent.get().getService(Service.Names.METADATA);
        return create("OkHttp", service.getOkHttpVersion());
    }

    public static ElasticTracer androidActivity() {
        return create("Android Activity");
    }

    public static ElasticTracer androidFragment() {
        return create("Android Fragment");
    }

    public static ElasticTracer coroutine() {
        return create("Kotlin Coroutine");
    }

    @Override
    public SpanBuilder spanBuilder(@NonNull String spanName) {
        return tracer.spanBuilder(spanName);
    }
}
