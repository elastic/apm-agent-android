package co.elastic.apm.compile.tools.publishing.subprojects;

import com.gradle.publish.PluginBundleExtension;
import com.gradle.publish.PublishPlugin;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;

import java.util.ArrayList;
import java.util.List;

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
        configureGradlePluginPortalBundle(project.getExtensions().getByType(PluginBundleExtension.class));
    }

    private void configureGradlePluginPortalBundle(PluginBundleExtension pluginBundle) {
        pluginBundle.setWebsite(getWebsiteUrl());
        pluginBundle.setVcsUrl(getRepositoryUrl());
        pluginBundle.setDescription(project.getDescription());
        List<String> tags = new ArrayList<>();
        tags.add("Android");
        tags.add("APM");
        tags.add("Elastic");
        tags.add("ELK");
        tags.add("opentelemetry");
        pluginBundle.setTags(tags);
    }

    private boolean isAGradlePluginProject(Project project) {
        return project.getPlugins().hasPlugin(GRADLE_PLUGIN_PROJECT_PLUGIN);
    }
}
