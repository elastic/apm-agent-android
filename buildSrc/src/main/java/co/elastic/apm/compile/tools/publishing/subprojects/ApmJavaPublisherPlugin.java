package co.elastic.apm.compile.tools.publishing.subprojects;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;

public class ApmJavaPublisherPlugin extends BaseApmPublisherPlugin {

    @Override
    public void apply(Project project) {
        project.afterEvaluate(self -> {
            if (!isAGradlePluginProject(self)) { // The "Gradle Plugin" plugin already creates its maven publication.
                JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
                java.withJavadocJar();
                java.withSourcesJar();
                addMavenPublication(self, "java");
            }
        });
    }

    private boolean isAGradlePluginProject(Project project) {
        return project.getPlugins().hasPlugin("java-gradle-plugin");
    }
}
