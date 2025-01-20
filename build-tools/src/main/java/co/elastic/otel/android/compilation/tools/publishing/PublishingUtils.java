package co.elastic.otel.android.compilation.tools.publishing;

import org.gradle.api.Project;

import java.util.Locale;

public class PublishingUtils {

    public static boolean isRelease(Project project) {
        String propertyName = "release";
        if (!project.hasProperty(propertyName)) {
            return false;
        }
        String release = (String) project.property(propertyName);
        return release.toLowerCase(Locale.US).equals("true");
    }
}
