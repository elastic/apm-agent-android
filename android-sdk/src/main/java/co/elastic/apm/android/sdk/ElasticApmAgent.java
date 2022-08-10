package co.elastic.apm.android.sdk;

import android.content.Context;
import android.os.Build;

import java.lang.reflect.Field;

import co.elastic.apm.android.sdk.traces.http.HttpSpanConfiguration;
import co.elastic.apm.android.sdk.traces.otel.exporter.ElasticSpanExporter;
import co.elastic.apm.android.sdk.traces.otel.processor.ElasticSpanProcessor;
import io.opentelemetry.api.GlobalOpenTelemetry;
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
        Attributes resourceAttributes = Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, appContext.getPackageName())
                .put(ResourceAttributes.SERVICE_VERSION, getServiceVersion())
                .put("telemetry.sdk.name", "android")
                .put("telemetry.sdk.version", BuildConfig.APM_AGENT_VERSION)
                .put("telemetry.sdk.language", "java")
                .put(ResourceAttributes.OS_DESCRIPTION, getOsDescription())
                .build();
        Resource resource = Resource.getDefault()
                .merge(Resource.create(resourceAttributes));

        ElasticSpanProcessor processor = new ElasticSpanProcessor(BatchSpanProcessor.builder(getSpanExporter()).build());
        processor.addAllExclusionRules(httpSpanConfiguration.exclusionRules);

        return SdkTracerProvider.builder()
                .addSpanProcessor(processor)
                .setResource(resource)
                .build();
    }

    private String getOsDescription() {
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("Android ");
        descriptionBuilder.append(Build.VERSION.CODENAME);
        descriptionBuilder.append(", API ");
        descriptionBuilder.append(Build.VERSION.SDK_INT);
        descriptionBuilder.append(", RELEASE ");
        descriptionBuilder.append(Build.VERSION.RELEASE);
        descriptionBuilder.append(", BUILD ");
        descriptionBuilder.append(Build.VERSION.INCREMENTAL);
        return descriptionBuilder.toString();
    }

    private String getServiceVersion() {
        try {
            Class<?> serviceBuildConfig = Class.forName(appContext.getPackageName() + ".BuildConfig");
            Field versionNameField = serviceBuildConfig.getDeclaredField("VERSION_NAME");
            return (String) versionNameField.get(serviceBuildConfig);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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