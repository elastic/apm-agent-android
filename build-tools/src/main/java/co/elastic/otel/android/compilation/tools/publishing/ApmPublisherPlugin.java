package co.elastic.otel.android.compilation.tools.publishing;

import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

import co.elastic.otel.android.compilation.tools.base.BaseProjectTypePlugin;
import co.elastic.otel.android.compilation.tools.publishing.subprojects.ApmAndroidPublisherPlugin;
import co.elastic.otel.android.compilation.tools.publishing.subprojects.ApmJavaPublisherPlugin;

public class ApmPublisherPlugin extends BaseProjectTypePlugin {

    @Override
    protected void onAndroidLibraryFound() {
        PluginContainer plugins = project.getPlugins();
        applyCommonPlugins(plugins);
        plugins.apply(ApmAndroidPublisherPlugin.class);
    }

    @Override
    protected void onJavaLibraryFound() {
        PluginContainer plugins = project.getPlugins();
        applyCommonPlugins(plugins);
        plugins.apply(ApmJavaPublisherPlugin.class);
    }

    private void applyCommonPlugins(PluginContainer plugins) {
        plugins.apply(MavenPublishPlugin.class);
        if (PublishingUtils.isRelease(project)) {
            plugins.apply(SigningPlugin.class);
            SigningExtension signing = project.getExtensions().getByType(SigningExtension.class);
            signing.useInMemoryPgpKeys(System.getenv("SECRING_ASC"), System.getenv("KEYPASS_SECRET"));
        }
    }
}
