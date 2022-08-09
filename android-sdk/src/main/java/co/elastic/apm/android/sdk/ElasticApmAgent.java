package co.elastic.apm.android.sdk;

import android.content.Context;

import co.elastic.apm.android.sdk.traces.http.HttpSpanConfiguration;
import co.elastic.apm.android.sdk.traces.otel.exporter.ElasticSpanExporter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public final class ElasticApmAgent {

    private static ElasticApmAgent instance;
    private final Context appContext;
    private final String endpoint;
    private final HttpSpanConfiguration httpSpanConfiguration;
    private Tracer tracer;

    public static Builder builder(Context appContext) {
        return new Builder(appContext);
    }

    public static ElasticApmAgent get() {
        verifyInitialization();
        return instance;
    }

    public void initialize() {
        OpenTelemetrySdk.builder()
                .setTracerProvider(getTracerProvider())
                .setPropagators(getContextPropagator())
                .buildAndRegisterGlobal();
        instance = this;
    }

    public Tracer getTracer() {
        if (tracer == null) {
            verifyInitialization();
            tracer = GlobalOpenTelemetry.getTracer("ElasticApmAgent-tracer");
        }

        return tracer;
    }

    public HttpSpanConfiguration getHttpSpanConfiguration() {
        return httpSpanConfiguration;
    }

    private ElasticApmAgent(Builder builder) {
        appContext = builder.appContext;
        endpoint = builder.endpoint;
        httpSpanConfiguration = builder.httpSpanConfiguration;
    }

    private SdkTracerProvider getTracerProvider() {
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, appContext.getPackageName())))
                .merge(Resource.create(Attributes.of(AttributeKey.stringKey("telemetry.sdk.name"), "android")))
                .merge(Resource.create(Attributes.of(AttributeKey.stringKey("telemetry.sdk.language"), "java")));

        return SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(getSpanExporter()).build())
                .setResource(resource)
                .build();
    }

    private SpanExporter getSpanExporter() {
        SpanExporter original;
        if (endpoint == null) {
            original = OtlpGrpcSpanExporter.getDefault();
        } else {
            original = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();
        }

        return new ElasticSpanExporter(original);
    }

    private ContextPropagators getContextPropagator() {
        return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    }

    private static void verifyInitialization() {
        if (instance == null) {
            throw new IllegalStateException("ElasticApmAgent hasn't been initialized");
        }
    }

    public static class Builder {
        private final Context appContext;
        private HttpSpanConfiguration httpSpanConfiguration;
        private String endpoint;

        private Builder(Context appContext) {
            this.appContext = appContext.getApplicationContext();
        }

        public Builder setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder setHttpSpanConfiguration(HttpSpanConfiguration httpSpanConfiguration) {
            this.httpSpanConfiguration = httpSpanConfiguration;
            return this;
        }

        public ElasticApmAgent build() {
            if (httpSpanConfiguration == null) {
                httpSpanConfiguration = HttpSpanConfiguration.builder().build();
            }
            return new ElasticApmAgent(this);
        }
    }
}