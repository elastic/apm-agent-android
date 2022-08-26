package co.elastic.apm.android.sdk;

import co.elastic.apm.android.sdk.services.Service;
import co.elastic.apm.android.sdk.services.ServiceManager;
import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
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

    public final ElasticApmConfiguration configuration;
    private static ElasticApmAgent instance;
    private final Connectivity connectivity;
    private final ServiceManager serviceManager;
    private Tracer tracer;

    public static ElasticApmAgent get() {
        verifyInitialization();
        return instance;
    }

    public synchronized static void initialize(Connectivity connectivity, ElasticApmConfiguration configuration) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        instance = new ElasticApmAgent(connectivity, configuration);
    }

    private static void verifyInitialization() {
        if (instance == null) {
            throw new IllegalStateException("ElasticApmAgent hasn't been initialized");
        }
    }

    public void destroy() {
        serviceManager.stop();
        instance = null;
    }

    public SpanBuilder spanBuilder(String spanName) {
        return getTracer().spanBuilder(spanName);
    }

    public <T extends Service> T getService(String name) {
        return serviceManager.getService(name);
    }

    ElasticApmAgent(Connectivity connectivity, ElasticApmConfiguration configuration) {
        this.connectivity = connectivity;
        this.configuration = configuration;
        serviceManager = configuration.serviceManager;
        serviceManager.start();
        initializeOpentelemetry();
    }

    private void initializeOpentelemetry() {
        OpenTelemetrySdk.builder()
                .setTracerProvider(getTracerProvider())
                .setPropagators(getContextPropagator())
                .buildAndRegisterGlobal();
    }

    private SdkTracerProvider getTracerProvider() {
        Resource resource = Resource.getDefault()
                .merge(configuration.globalAttributes.provideAsResource());

        ElasticSpanProcessor processor = new ElasticSpanProcessor(BatchSpanProcessor.builder(getSpanExporter()).build());
        processor.addAllExclusionRules(configuration.httpSpanConfiguration.exclusionRules);

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
}