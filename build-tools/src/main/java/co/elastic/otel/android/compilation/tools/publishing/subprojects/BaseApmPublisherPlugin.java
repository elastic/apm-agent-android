package co.elastic.otel.android.compilation.tools.publishing.subprojects;

import static co.elastic.otel.android.compilation.tools.publishing.PublishingUtils.getArtifactId;
import static co.elastic.otel.android.compilation.tools.publishing.PublishingUtils.getGroupId;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.plugins.signing.SigningExtension;

import co.elastic.otel.android.compilation.tools.publishing.PublishingUtils;

public abstract class BaseApmPublisherPlugin implements Plugin<Project> {
    protected Project project;
    private PublishingExtension mavenPublishExtension;

    @Override
    public final void apply(Project project) {
        this.project = project;
        mavenPublishExtension = project.getExtensions().getByType(PublishingExtension.class);
        onApply();
        mavenPublishExtension.getPublications().configureEach(publication -> {
            if (publication instanceof MavenPublication) {
                ((MavenPublication) publication).setArtifactId(getArtifactId(project));
                ((MavenPublication) publication).setGroupId(getGroupId(project));
            }
        });
    }

    protected abstract void onApply();

    protected void addMavenPublication(String componentName) {
        mavenPublishExtension.getPublications().create("elastic", MavenPublication.class, publication -> {
            publication.from(project.getComponents().findByName(componentName));
            configurePom(publication);
            if (isRelease()) {
                signPublication(publication);
            }
        });
    }

    private void configurePom(MavenPublication publication) {
        publication.pom(mavenPom -> {
            String repoUrl = getRepositoryUrl();
            String organizationName = "Elastic Inc.";
            String organizationUrl = getWebsiteUrl();
            mavenPom.getName().set(getGroupId(project) + ":" + getArtifactId(project));
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
    }

    private void signPublication(MavenPublication publication) {
        SigningExtension signing = project.getExtensions().getByType(SigningExtension.class);
        signing.sign(publication);
    }

    protected String getWebsiteUrl() {
        return "https://www.elastic.co";
    }

    protected String getRepositoryUrl() {
        return "https://github.com/elastic/apm-agent-android";
    }

    protected boolean isRelease() {
        return PublishingUtils.isRelease(project);
    }
}
