package co.elastic.apm.compile.tools;

import co.elastic.apm.compile.tools.base.BasePlugin;
import co.elastic.apm.compile.tools.plugins.subprojects.AarApmCompilerPlugin;
import co.elastic.apm.compile.tools.plugins.subprojects.JarApmCompilerPlugin;

public class ApmCompilerPlugin extends BasePlugin {

    @Override
    protected void onApply() {
        if (isAndroidProject()) {
            project.getPlugins().apply(AarApmCompilerPlugin.class);
        } else {
            project.getPlugins().apply(JarApmCompilerPlugin.class);
        }
    }
}
