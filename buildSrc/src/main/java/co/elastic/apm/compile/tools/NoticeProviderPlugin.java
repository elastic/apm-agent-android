package co.elastic.apm.compile.tools;

import co.elastic.apm.compile.tools.base.BasePlugin;
import co.elastic.apm.compile.tools.plugins.subprojects.AarNoticeProviderPlugin;
import co.elastic.apm.compile.tools.plugins.subprojects.JarNoticeProviderPlugin;

public class NoticeProviderPlugin extends BasePlugin {

    @Override
    protected void onApply() {
        if (isAndroidProject()) {
            project.getPlugins().apply(AarNoticeProviderPlugin.class);
        } else {
            project.getPlugins().apply(JarNoticeProviderPlugin.class);
        }
    }
}
