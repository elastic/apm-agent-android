package co.elastic.otel.android.compilation.tools.publishing;

import org.gradle.api.Project;

import java.util.Locale;
import java.util.Map;

public class PublishingUtils {
    private static final String PROPERTY_ARTIFACT_ID = "elastic.artifactId";
    private static final String PROPERTY_GROUP_ID = "elastic.groupId";

    public static String getArtifactId(Project project) {
        Map<String, Object> extraProperties = project.getExtensions().getExtraProperties().getProperties();
        return (String) extraProperties.getOrDefault(PROPERTY_ARTIFACT_ID, project.getName());
    }

    public static String getGroupId(Project project) {
        Map<String, Object> extraProperties = project.getExtensions().getExtraProperties().getProperties();
        return (String) extraProperties.getOrDefault(PROPERTY_GROUP_ID, project.getGroup());
    }

    public static void setArtifactId(Project project, String artifactId) {
        project.getExtensions().getExtraProperties().set(PROPERTY_ARTIFACT_ID, artifactId);
    }

    public static void setGroupId(Project project, String groupId) {
        project.getExtensions().getExtraProperties().set(PROPERTY_GROUP_ID, groupId);
    }

    public static boolean isRelease(Project project) {
        String propertyName = "release";
        if (!project.hasProperty(propertyName)) {
            return false;
        }
        String release = (String) project.property(propertyName);
        return release.toLowerCase(Locale.US).equals("true");
    }
}
