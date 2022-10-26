package co.elastic.apm.compile.tools.publishing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;

import co.elastic.apm.compile.tools.NoticeProviderPlugin;
import co.elastic.apm.compile.tools.plugins.RootNoticeProviderPlugin;
import co.elastic.apm.compile.tools.sourceheader.ApmSourceHeaderPlugin;

public class ApmPublisherRootPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(RootNoticeProviderPlugin.class);
        project.subprojects(subproject -> {
            PluginContainer plugins = subproject.getPlugins();
            plugins.apply(ApmSourceHeaderPlugin.class);
            plugins.apply(NoticeProviderPlugin.class);
            plugins.apply(ApmPublisherPlugin.class);
            project.getDependencies().add("noticeProducer", subproject);
        });
    }
}
