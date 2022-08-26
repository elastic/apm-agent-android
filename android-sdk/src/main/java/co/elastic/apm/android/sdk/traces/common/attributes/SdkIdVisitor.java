package co.elastic.apm.android.sdk.traces.common.attributes;

import co.elastic.apm.android.sdk.BuildConfig;
import co.elastic.apm.android.sdk.attributes.AttributesBuilderVisitor;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class SdkIdVisitor implements AttributesBuilderVisitor {

    @Override
    public void visit(AttributesBuilder builder) {
        builder.put(ResourceAttributes.TELEMETRY_SDK_NAME, "android")
                .put(ResourceAttributes.TELEMETRY_SDK_VERSION, BuildConfig.APM_AGENT_VERSION)
                .put(ResourceAttributes.TELEMETRY_SDK_LANGUAGE, "java");
    }
}
