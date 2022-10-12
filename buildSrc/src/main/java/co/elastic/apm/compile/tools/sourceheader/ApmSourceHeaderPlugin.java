package co.elastic.apm.compile.tools.sourceheader;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import co.elastic.apm.compile.tools.sourceheader.subplugins.AndroidSourceHeaderPlugin;
import co.elastic.apm.compile.tools.sourceheader.subplugins.JavaSourceHeaderPlugin;

public class ApmSourceHeaderPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (isAndroidProject(project)) {
            project.getPluginManager().apply(AndroidSourceHeaderPlugin.class);
        } else {
            project.getPluginManager().apply(JavaSourceHeaderPlugin.class);
        }
    }

    private boolean isAndroidProject(Project project) {
        return project.getExtensions().findByName("android") != null;
    }
}
