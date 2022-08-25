package co.elastic.apm.android.sdk.traces.common.attributes;

import co.elastic.apm.android.sdk.BuildConfig;
import co.elastic.apm.android.sdk.data.attributes.AttributesBuilderVisitor;
import io.opentelemetry.api.common.AttributesBuilder;

public class SdkIdVisitor implements AttributesBuilderVisitor {

    @Override
    public void visit(AttributesBuilder builder) {
        builder.put("telemetry.sdk.name", "android")
                .put("telemetry.sdk.version", BuildConfig.APM_AGENT_VERSION)
                .put("telemetry.sdk.language", "java");
    }
}
