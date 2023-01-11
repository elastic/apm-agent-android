package co.elastic.apm.android.agp.api.tools;

import com.android.build.api.variant.Variant;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;

public interface ClasspathProvider {
    FileCollection getRuntimeClasspath(Variant variant);

    Configuration getRuntimeConfiguration(Variant variant);
}
