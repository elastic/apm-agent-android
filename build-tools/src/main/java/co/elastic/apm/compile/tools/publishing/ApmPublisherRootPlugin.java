package co.elastic.apm.compile.tools.publishing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;

import java.time.Duration;

import co.elastic.apm.compile.tools.NoticeProviderPlugin;
import co.elastic.apm.compile.tools.plugins.RootNoticeProviderPlugin;
import co.elastic.apm.compile.tools.publishing.tasks.PostDeployTask;
import co.elastic.apm.compile.tools.sourceheader.ApmSourceHeaderPlugin;
import io.github.gradlenexus.publishplugin.NexusPublishExtension;
import io.github.gradlenexus.publishplugin.NexusPublishPlugin;
import io.github.gradlenexus.publishplugin.NexusRepositoryContainer;

public class ApmPublisherRootPlugin implements Plugin<Project> {

    private final static String PROPERTY_VERSION_OVERRIDE = "version_override";

    @Override
    public void apply(Project project) {
        configureVersion(project);
        applyRootPlugins(project.getPlugins());
        addPostDeployTask(project);
        configureMavenCentral(project);
        project.subprojects(subproject -> {
            applySubprojectPlugins(subproject.getPlugins());
            project.getDependencies().add("noticeProducer", subproject);
        });
    }

    private void configureVersion(Project project) {
        String versionOverride = getVersionOverride(project);
        if (versionOverride != null) {
            System.out.println("Overriding version with: '" + versionOverride + "'");
            project.setVersion(versionOverride);
            project.subprojects(subproject -> subproject.setVersion(versionOverride));
        }
    }

    private String getVersionOverride(Project project) {
        if (!project.hasProperty(PROPERTY_VERSION_OVERRIDE)) {
            return null;
        }
        String property = (String) project.property(PROPERTY_VERSION_OVERRIDE);
        if (property == null) {
            return null;
        }
        property = property.trim();
        if (property.isEmpty()) {
            return null;
        }
        return property;
    }

    private void addPostDeployTask(Project project) {
        project.getTasks().register("postDeploy", PostDeployTask.class);
    }

    private void applySubprojectPlugins(PluginContainer subprojectPlugins) {
        subprojectPlugins.apply(ApmSourceHeaderPlugin.class);
        subprojectPlugins.apply(NoticeProviderPlugin.class);
        subprojectPlugins.apply(ApmPublisherPlugin.class);
    }

    private void applyRootPlugins(PluginContainer plugins) {
        plugins.apply(RootNoticeProviderPlugin.class);
        plugins.apply(NexusPublishPlugin.class);
    }

    private void configureMavenCentral(Project project) {
        NexusPublishExtension nexusPublishExtension = project.getExtensions().getByType(NexusPublishExtension.class);
        nexusPublishExtension.repositories(NexusRepositoryContainer::sonatype);
        nexusPublishExtension.getClientTimeout().set(Duration.ofMinutes(10));
        nexusPublishExtension.getConnectTimeout().set(Duration.ofMinutes(10));
        nexusPublishExtension.transitionCheckOptions(transitionCheckOptions -> {
            transitionCheckOptions.getMaxRetries().set(200);
            transitionCheckOptions.getDelayBetween().set(Duration.ofSeconds(15));
        });
    }
}
