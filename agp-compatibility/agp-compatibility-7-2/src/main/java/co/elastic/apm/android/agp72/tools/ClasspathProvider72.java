package co.elastic.apm.android.agp72.tools;

import com.android.build.api.component.impl.ComponentImpl;
import com.android.build.api.variant.Variant;
import com.android.build.gradle.internal.publishing.AndroidArtifacts;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;

import co.elastic.apm.android.agp.api.tools.ClasspathProvider;

public class ClasspathProvider72 implements ClasspathProvider {
    private FileCollection runtimeClasspath;

    @Override
    public FileCollection getRuntimeClasspath(Variant variant) {
        if (runtimeClasspath == null) {
            runtimeClasspath = ((ComponentImpl) variant).getVariantDependencies().getArtifactFileCollection(AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
                    AndroidArtifacts.ArtifactScope.ALL,
                    AndroidArtifacts.ArtifactType.CLASSES_JAR);
        }

        return runtimeClasspath;
    }

    @Override
    public Configuration getRuntimeConfiguration(Variant variant) {
        return ((ComponentImpl) variant).getVariantDependencies().getRuntimeClasspath();
    }
}
