package co.elastic.apm.android.agp73.tools;

import com.android.build.api.variant.Variant;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.file.FileCollection;

import co.elastic.apm.android.agp.api.tools.ClasspathProvider;

public class ClasspathProvider73 implements ClasspathProvider, Action<ArtifactView.ViewConfiguration> {

    private static final Attribute<String> ARTIFACT_TYPE_ATTR = Attribute.of("artifactType", String.class);
    private FileCollection runtimeClasspath;

    @Override
    public FileCollection getRuntimeClasspath(Variant variant) {
        if (runtimeClasspath == null) {
            runtimeClasspath = findClasspath(variant);
        }

        return runtimeClasspath;
    }

    @Override
    public Configuration getRuntimeConfiguration(Variant variant) {
        return variant.getRuntimeConfiguration();
    }

    private FileCollection findClasspath(Variant variant) {
        return getRuntimeConfiguration(variant).getIncoming()
                .artifactView(this)
                .getArtifacts()
                .getArtifactFiles();
    }

    @Override
    public void execute(ArtifactView.ViewConfiguration configuration) {
        configuration.setLenient(false);
        configuration.getAttributes().attribute(ARTIFACT_TYPE_ATTR, "android-classes-jar");
    }
}
