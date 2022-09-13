package co.elastic.apm.compile.tools;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import co.elastic.apm.compile.tools.subplugins.AarApmCompilerPlugin;
import co.elastic.apm.compile.tools.subplugins.JarApmCompilerPlugin;

public class ApmCompilerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (isAndroidProject(project)) {
            project.getPlugins().apply(AarApmCompilerPlugin.class);
        } else {
            project.getPlugins().apply(JarApmCompilerPlugin.class);
        }
    }

    private boolean isAndroidProject(Project project) {
        return project.getExtensions().findByName("android") != null;
    }
}
