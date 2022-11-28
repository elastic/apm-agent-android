package co.elastic.apm.compile.tools.publishing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;

import co.elastic.apm.compile.tools.NoticeProviderPlugin;
import co.elastic.apm.compile.tools.plugins.RootNoticeProviderPlugin;
import co.elastic.apm.compile.tools.publishing.tasks.PostDeployTask;
import co.elastic.apm.compile.tools.sourceheader.ApmSourceHeaderPlugin;
import io.github.gradlenexus.publishplugin.NexusPublishExtension;
import io.github.gradlenexus.publishplugin.NexusPublishPlugin;
import io.github.gradlenexus.publishplugin.NexusRepositoryContainer;

public class ApmPublisherRootPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        applyRootPlugins(project.getPlugins());
        addPostDeployTask(project);
        configureMavenCentral(project);
        project.subprojects(subproject -> {
            applySubprojectPlugins(subproject.getPlugins());
            project.getDependencies().add("noticeProducer", subproject);
        });
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
    }
}
