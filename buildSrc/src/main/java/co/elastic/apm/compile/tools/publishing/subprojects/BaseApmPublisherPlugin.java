package co.elastic.apm.compile.tools.publishing.subprojects;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;

public abstract class BaseApmPublisherPlugin implements Plugin<Project> {

    protected void addMavenPublication(Project project, String componentName) {
        PublishingExtension mavenPublishExtension = project.getExtensions().getByType(PublishingExtension.class);
        mavenPublishExtension.getPublications().create("elasticPublication", MavenPublication.class, publication -> {
            publication.from(project.getComponents().findByName(componentName));
        });
    }
}
