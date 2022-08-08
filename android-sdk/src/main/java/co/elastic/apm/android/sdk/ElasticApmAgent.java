package co.elastic.apm.android.sdk;

import android.content.Context;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class ElasticApmAgent {

    public void initialize(Context context) {
        OpenTelemetrySdk.builder()
                .setTracerProvider(getTracerProvider(context))
                .setPropagators(getContextPropagator())
                .buildAndRegisterGlobal();
    }

    private SdkTracerProvider getTracerProvider(Context androidContext) {
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, androidContext.getPackageName())))
                .merge(Resource.create(Attributes.of(AttributeKey.stringKey("telemetry.sdk.name"), "android")))
                .merge(Resource.create(Attributes.of(AttributeKey.stringKey("telemetry.sdk.language"), "java")));

        return SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(OtlpGrpcSpanExporter.getDefault()))
                .setResource(resource)
                .build();
    }

    private ContextPropagators getContextPropagator() {
        return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    }
}