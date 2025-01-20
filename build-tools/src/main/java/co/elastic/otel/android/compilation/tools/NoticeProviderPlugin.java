package co.elastic.otel.android.compilation.tools;

import co.elastic.otel.android.compilation.tools.base.BaseProjectTypePlugin;
import co.elastic.otel.android.compilation.tools.plugins.subprojects.AarNoticeProviderPlugin;
import co.elastic.otel.android.compilation.tools.plugins.subprojects.JarNoticeProviderPlugin;

public class NoticeProviderPlugin extends BaseProjectTypePlugin {

    @Override
    protected void onAndroidLibraryFound() {
        project.getPlugins().apply(AarNoticeProviderPlugin.class);
    }

    @Override
    protected void onJavaLibraryFound() {
        project.getPlugins().apply(JarNoticeProviderPlugin.class);
    }
}
