package co.elastic.apm.compile.tools.publishing.subprojects;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;

import java.util.Locale;

public abstract class BaseApmPublisherPlugin implements Plugin<Project> {
    protected Project project;

    @Override
    public final void apply(Project project) {
        this.project = project;
        onApply();
    }

    protected abstract void onApply();

    protected void addMavenPublication(String componentName) {
        PublishingExtension mavenPublishExtension = project.getExtensions().getByType(PublishingExtension.class);
        mavenPublishExtension.getPublications().create("elasticPublication", MavenPublication.class, publication -> {
            publication.from(project.getComponents().findByName(componentName));
            publication.pom(mavenPom -> {
                String repoUrl = "https://github.com/elastic/apm-agent-android";
                String organizationName = "Elastic Inc.";
                String organizationUrl = "https://www.elastic.co";
                mavenPom.getName().set(project.getGroup() + ":" + project.getName());
                mavenPom.getDescription().set(project.getDescription());
                mavenPom.getUrl().set(repoUrl);
                mavenPom.organization(organization -> {
                    organization.getName().set(organizationName);
                    organization.getUrl().set(organizationUrl);
                });
                mavenPom.licenses(spec -> spec.license(license -> {
                    license.getName().set("The Apache Software License, Version 2.0");
                    license.getUrl().set("http://www.apache.org/licenses/LICENSE-2.0.txt");
                }));
                mavenPom.scm(scm -> {
                    String scmUrl = "scm:git:git@github.com:elastic/apm-agent-android.git";
                    scm.getConnection().set(scmUrl);
                    scm.getDeveloperConnection().set(scmUrl);
                    scm.getUrl().set(repoUrl);
                    scm.getTag().set("HEAD");
                });
                mavenPom.developers(spec -> spec.developer(developer -> {
                    developer.getName().set("Elastic");
                    developer.getUrl().set("https://discuss.elastic.co/c/apm");
                    developer.getOrganization().set(organizationName);
                    developer.getOrganizationUrl().set(organizationUrl);
                }));
            });
        });
    }

    protected boolean isRelease() {
        String propertyName = "release";
        if (!project.hasProperty(propertyName)) {
            return false;
        }
        String release = (String) project.property(propertyName);
        return release.toLowerCase(Locale.US).equals("true");
    }
}
