package co.elastic.apm.android.sdk;

import android.content.Context;

import co.elastic.apm.android.sdk.attributes.AttributesCompose;
import co.elastic.apm.android.sdk.services.Service;
import co.elastic.apm.android.sdk.services.ServiceManager;
import co.elastic.apm.android.sdk.services.network.NetworkService;
import co.elastic.apm.android.sdk.services.permissions.AndroidPermissionService;
import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import co.elastic.apm.android.sdk.traces.http.HttpSpanConfiguration;
import co.elastic.apm.android.sdk.traces.otel.exporter.ElasticSpanExporter;
import co.elastic.apm.android.sdk.traces.otel.processor.ElasticSpanProcessor;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public final class ElasticApmAgent {

    private static ElasticApmAgent instance;
    private final Connectivity connectivity;
    private final HttpSpanConfiguration httpSpanConfiguration;
    private final AttributesCompose globalAttributes;
    private final ServiceManager serviceManager;
    private Tracer tracer;

    public static Builder builder(Context appContext, Connectivity connectivity) {
        return new Builder(appContext, connectivity);
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
        serviceManager.start();
        instance = this;
    }

    public void destroy() {
        serviceManager.stop();
        instance = null;
    }

    public SpanBuilder spanBuilder(String spanName) {
        return getTracer().spanBuilder(spanName);
    }

    public HttpSpanConfiguration getHttpSpanConfiguration() {
        return httpSpanConfiguration;
    }

    public <T extends Service> T getService(String name) {
        return serviceManager.getService(name);
    }

    private ElasticApmAgent(Builder builder) {
        connectivity = builder.connectivity;
        globalAttributes = builder.globalAttributes;
        httpSpanConfiguration = builder.httpSpanConfiguration;
        serviceManager = builder.serviceManager;
    }

    private SdkTracerProvider getTracerProvider() {
        Resource resource = Resource.getDefault()
                .merge(globalAttributes.provideAsResource());

        ElasticSpanProcessor processor = new ElasticSpanProcessor(BatchSpanProcessor.builder(getSpanExporter()).build());
        processor.addAllExclusionRules(httpSpanConfiguration.exclusionRules);

        return SdkTracerProvider.builder()
                .addSpanProcessor(processor)
                .setResource(resource)
                .build();
    }

    private SpanExporter getSpanExporter() {
        return new ElasticSpanExporter(connectivity.getSpanExporter());
    }

    private ContextPropagators getContextPropagator() {
        return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    }

    private Tracer getTracer() {
        if (tracer == null) {
            verifyInitialization();
            tracer = GlobalOpenTelemetry.getTracer("ElasticApmAgent-tracer");
        }

        return tracer;
    }

    private static void verifyInitialization() {
        if (instance == null) {
            throw new IllegalStateException("ElasticApmAgent hasn't been initialized");
        }
    }

    public static class Builder {
        private final AttributesCompose globalAttributes;
        private final ServiceManager serviceManager;
        private final Connectivity connectivity;
        private HttpSpanConfiguration httpSpanConfiguration;

        private Builder(Context appContext, Connectivity connectivity) {
            this.connectivity = connectivity;
            globalAttributes = AttributesCompose.global(appContext);
            serviceManager = new ServiceManager();
            serviceManager.addService(new NetworkService(appContext));
            serviceManager.addService(new AndroidPermissionService(appContext));
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