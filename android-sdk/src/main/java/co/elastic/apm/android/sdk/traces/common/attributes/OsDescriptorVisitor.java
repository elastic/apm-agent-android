package co.elastic.apm.android.sdk.traces.common.attributes;

import android.os.Build;

import co.elastic.apm.android.sdk.attributes.AttributesBuilderVisitor;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class OsDescriptorVisitor implements AttributesBuilderVisitor {

    @Override
    public void visit(AttributesBuilder builder) {
        builder.put(ResourceAttributes.OS_DESCRIPTION, getOsDescription())
                .put(ResourceAttributes.OS_TYPE, "linux")
                .put(ResourceAttributes.OS_VERSION, Build.VERSION.RELEASE)
                .put(ResourceAttributes.OS_NAME, Build.VERSION.CODENAME);
    }

    private String getOsDescription() {
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("Android ");
        descriptionBuilder.append(Build.VERSION.RELEASE);
        descriptionBuilder.append(", API level ");
        descriptionBuilder.append(Build.VERSION.SDK_INT);
        descriptionBuilder.append(", NAME ");
        descriptionBuilder.append(Build.VERSION.CODENAME);
        descriptionBuilder.append(", BUILD ");
        descriptionBuilder.append(Build.VERSION.INCREMENTAL);
        return descriptionBuilder.toString();
    }
}