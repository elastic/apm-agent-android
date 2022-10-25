package co.elastic.apm.compile.tools.publishing.extension;

import org.gradle.api.provider.Property;

public abstract class TargetProjectExtension {
    public abstract Property<Boolean> getDisablePublication();

    public TargetProjectExtension() {
        getDisablePublication().convention(false);
    }
}
