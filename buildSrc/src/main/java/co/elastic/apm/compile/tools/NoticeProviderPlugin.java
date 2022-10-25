package co.elastic.apm.compile.tools;

import co.elastic.apm.compile.tools.base.BaseProjectTypePlugin;
import co.elastic.apm.compile.tools.plugins.subprojects.AarNoticeProviderPlugin;
import co.elastic.apm.compile.tools.plugins.subprojects.JarNoticeProviderPlugin;

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
