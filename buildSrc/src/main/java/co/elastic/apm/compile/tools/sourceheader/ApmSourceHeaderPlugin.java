package co.elastic.apm.compile.tools.sourceheader;

import co.elastic.apm.compile.tools.base.BasePlugin;
import co.elastic.apm.compile.tools.sourceheader.subplugins.AndroidSourceHeaderPlugin;
import co.elastic.apm.compile.tools.sourceheader.subplugins.JavaSourceHeaderPlugin;

public class ApmSourceHeaderPlugin extends BasePlugin {

    @Override
    protected void onApply() {
        if (isAndroidProject()) {
            project.getPluginManager().apply(AndroidSourceHeaderPlugin.class);
        } else {
            project.getPluginManager().apply(JavaSourceHeaderPlugin.class);
        }
    }
}
