package co.elastic.apm.android.sdk.data.attributes;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.elastic.apm.android.sdk.data.attributes.visitors.DeviceIdVisitor;
import co.elastic.apm.android.sdk.data.attributes.visitors.DeviceInfoVisitor;
import co.elastic.apm.android.sdk.data.attributes.visitors.OsDescriptorVisitor;
import co.elastic.apm.android.sdk.data.attributes.visitors.SdkIdVisitor;
import co.elastic.apm.android.sdk.data.attributes.visitors.ServiceIdVisitor;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;

public final class AttributesCompose {
    private final List<AttributesBuilderVisitor> visitors = new ArrayList<>();

    public static AttributesCompose create(AttributesBuilderVisitor... visitors) {
        return new AttributesCompose(Arrays.asList(visitors));
    }

    public static AttributesCompose global(Context appContext) {
        return create(new DeviceIdVisitor(appContext),
                new DeviceInfoVisitor(),
                new OsDescriptorVisitor(),
                new SdkIdVisitor(),
                new ServiceIdVisitor(appContext));
    }

    public AttributesCompose(List<AttributesBuilderVisitor> defaultVisitors) {
        if (defaultVisitors != null) {
            visitors.addAll(defaultVisitors);
        }
    }

    public void addVisitor(AttributesBuilderVisitor visitor) {
        if (visitor == null) {
            throw new NullPointerException();
        }
        visitors.add(visitor);
    }

    public Attributes provide() {
        AttributesBuilder builder = Attributes.builder();

        for (AttributesBuilderVisitor visitor : visitors) {
            visitor.visit(builder);
        }

        return builder.build();
    }

    public Resource provideAsResource() {
        return Resource.create(provide());
    }
}
