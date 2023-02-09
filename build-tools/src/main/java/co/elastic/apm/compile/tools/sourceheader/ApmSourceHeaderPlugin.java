package co.elastic.apm.compile.tools.sourceheader;

import co.elastic.apm.compile.tools.base.BaseProjectTypePlugin;
import co.elastic.apm.compile.tools.sourceheader.subplugins.AndroidSourceHeaderPlugin;
import co.elastic.apm.compile.tools.sourceheader.subplugins.JavaSourceHeaderPlugin;

public class ApmSourceHeaderPlugin extends BaseProjectTypePlugin {

    @Override
    protected void onAndroidLibraryFound() {
        project.getPluginManager().apply(AndroidSourceHeaderPlugin.class);
    }

    @Override
    protected void onJavaLibraryFound() {
        project.getPluginManager().apply(JavaSourceHeaderPlugin.class);
    }
}
