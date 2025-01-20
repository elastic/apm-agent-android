package co.elastic.otel.android.compilation.tools.sourceheader;

import co.elastic.otel.android.compilation.tools.base.BaseProjectTypePlugin;
import co.elastic.otel.android.compilation.tools.sourceheader.subplugins.AndroidSourceHeaderPlugin;
import co.elastic.otel.android.compilation.tools.sourceheader.subplugins.JavaSourceHeaderPlugin;

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
