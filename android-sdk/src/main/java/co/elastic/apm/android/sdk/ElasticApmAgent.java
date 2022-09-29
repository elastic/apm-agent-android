package co.elastic.apm.android.sdk;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.sdk.attributes.AttributesCompose;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import co.elastic.apm.android.sdk.internal.services.network.NetworkService;
import co.elastic.apm.android.sdk.internal.services.permissions.AndroidPermissionService;
import co.elastic.apm.android.sdk.traces.common.rules.DiscardExclusionRule;
import co.elastic.apm.android.sdk.traces.connectivity.Connectivity;
import co.elastic.apm.android.sdk.traces.otel.processor.ElasticSpanProcessor;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;

public final class ElasticApmAgent {

    public final ElasticApmConfiguration configuration;
    private static ElasticApmAgent instance;
    private final Connectivity connectivity;
    private final ServiceManager serviceManager;
    private final AttributesCompose globalAttributes;

    public static ElasticApmAgent get() {
        verifyInitialization();
        return instance;
    }

    public synchronized static ElasticApmAgent initialize(Context context, Connectivity connectivity) {
        return initialize(context, connectivity, ElasticApmConfiguration.getDefault());
    }

    public synchronized static ElasticApmAgent initialize(Context context, Connectivity connectivity, ElasticApmConfiguration configuration) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        instance = new ElasticApmAgent(context, connectivity, configuration);
        instance.onInitializationFinished();
        return instance;
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

    public <T extends Service> T getService(String name) {
        return serviceManager.getService(name);
    }

    ElasticApmAgent(Context context, Connectivity connectivity, ElasticApmConfiguration configuration) {
        Context appContext = context.getApplicationContext();
        this.connectivity = connectivity;
        this.configuration = configuration;
        serviceManager = new ServiceManager();
        serviceManager.addService(new NetworkService(appContext));
        serviceManager.addService(new AndroidPermissionService(appContext));
        serviceManager.addService(new ApmMetadataService(appContext));
        globalAttributes = AttributesCompose.global(appContext, configuration.serviceName, configuration.serviceVersion);
    }

    private void onInitializationFinished() {
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
                .merge(globalAttributes.provideAsResource());

        ElasticSpanProcessor processor = getProcessor();
        List<ElasticSpanProcessor.ExclusionRule> exclusionRules = new ArrayList<>(configuration.httpTraceConfiguration.exclusionRules);
        exclusionRules.add(new DiscardExclusionRule());
        processor.addAllExclusionRules(exclusionRules);

        return SdkTracerProvider.builder()
                .addSpanProcessor(processor)
                .setResource(resource)
                .build();
    }

    @NonNull
    private ElasticSpanProcessor getProcessor() {
        SpanProcessor spanProcessor = connectivity.getSpanProcessor();
        if (spanProcessor instanceof ElasticSpanProcessor) {
            return (ElasticSpanProcessor) spanProcessor;
        }
        return new ElasticSpanProcessor(spanProcessor);
    }

    private ContextPropagators getContextPropagator() {
        return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    }
}