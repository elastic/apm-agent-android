package co.elastic.apm.compile.tools.publishing;

import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

import co.elastic.apm.compile.tools.NoticeProviderPlugin;
import co.elastic.apm.compile.tools.base.BasePlugin;
import co.elastic.apm.compile.tools.plugins.RootNoticeProviderPlugin;
import co.elastic.apm.compile.tools.publishing.subprojects.ApmAndroidPublisherPlugin;
import co.elastic.apm.compile.tools.publishing.subprojects.ApmJavaPublisherPlugin;
import co.elastic.apm.compile.tools.sourceheader.ApmSourceHeaderPlugin;

public class ApmPublisherRootPlugin extends BasePlugin {
    private static final String TARGET_PROJECT_EXTENSION_NAME = "publishingTarget";

    @Override
    protected void onApply() {
        project.getPlugins().apply(RootNoticeProviderPlugin.class);
        project.subprojects(subproject -> {
            PluginContainer plugins = subproject.getPlugins();
            applyPluginsToSubproject(plugins);
            if (isAndroidProject(subproject)) {
                plugins.apply(ApmAndroidPublisherPlugin.class);
            } else {
                plugins.apply(ApmJavaPublisherPlugin.class);
            }
        });
    }

    private void applyPluginsToSubproject(PluginContainer plugins) {
        plugins.apply(MavenPublishPlugin.class);
        plugins.apply(ApmSourceHeaderPlugin.class);
        plugins.apply(NoticeProviderPlugin.class);
    }
}
