package co.elastic.apm.compile.tools.publishing;

import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

import co.elastic.apm.compile.tools.base.BasePlugin;
import co.elastic.apm.compile.tools.publishing.extension.TargetProjectExtension;
import co.elastic.apm.compile.tools.publishing.subprojects.ApmAndroidPublisherPlugin;
import co.elastic.apm.compile.tools.publishing.subprojects.ApmJavaPublisherPlugin;
import co.elastic.apm.compile.tools.sourceheader.ApmSourceHeaderPlugin;

public class ApmPublisherRootPlugin extends BasePlugin {
    private static final String TARGET_PROJECT_EXTENSION_NAME = "publishingTarget";

    @Override
    protected void onApply() {
        project.subprojects(subproject -> {
            TargetProjectExtension extension = subproject.getExtensions().create(TARGET_PROJECT_EXTENSION_NAME, TargetProjectExtension.class);

            subproject.afterEvaluate(sameSubproject -> {
                if (!extension.getDisablePublication().get()) {
                    PluginContainer plugins = subproject.getPlugins();
                    plugins.apply(MavenPublishPlugin.class);
                    plugins.apply(ApmSourceHeaderPlugin.class);
                    if (isAndroidProject(subproject)) {
                        plugins.apply(ApmAndroidPublisherPlugin.class);
                    } else {
                        plugins.apply(ApmJavaPublisherPlugin.class);
                    }
                }
            });
        });
    }
}
