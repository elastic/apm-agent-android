package co.elastic.apm.compile.tools.notice.extensions;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class AndroidApmExtension {
    public final Property<String> variantName;

    @Inject
    public AndroidApmExtension(ObjectFactory objects) {
        variantName = objects.property(String.class);
        variantName.convention("release");
    }
}
