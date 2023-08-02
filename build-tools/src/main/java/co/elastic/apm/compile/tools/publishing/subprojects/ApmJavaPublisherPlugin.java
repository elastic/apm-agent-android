package co.elastic.apm.compile.tools.publishing.subprojects;

import com.gradle.publish.PublishPlugin;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public class ApmJavaPublisherPlugin extends BaseApmPublisherPlugin {
    private static final String GRADLE_PLUGIN_PROJECT_PLUGIN = "java-gradle-plugin";

    @Override
    protected void onApply() {
        project.getPlugins().withId(GRADLE_PLUGIN_PROJECT_PLUGIN, plugin -> configureGradlePluginPublication());
        project.afterEvaluate(self -> {
            if (!isAGradlePluginProject(self)) { // The "Gradle Plugin" plugin already creates its maven publication.
                JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
                if (isRelease()) {
                    java.withJavadocJar();
                    java.withSourcesJar();
                }
                addMavenPublication("java");
            }
        });
    }

    private void configureGradlePluginPublication() {
        project.getPlugins().apply(PublishPlugin.class);
        configureGradlePluginPortalBundle(project.getExtensions().getByType(GradlePluginDevelopmentExtension.class));
    }

    private void configureGradlePluginPortalBundle(GradlePluginDevelopmentExtension pluginBundle) {
        pluginBundle.getWebsite().set(getWebsiteUrl());
        pluginBundle.getVcsUrl().set(getRepositoryUrl());
    }

    private boolean isAGradlePluginProject(Project project) {
        return project.getPlugins().hasPlugin(GRADLE_PLUGIN_PROJECT_PLUGIN);
    }
}
