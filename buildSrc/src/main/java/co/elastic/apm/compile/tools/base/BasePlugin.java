package co.elastic.apm.compile.tools.base;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class BasePlugin implements Plugin<Project> {

    protected Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        onApply();
    }

    protected abstract void onApply();

    protected boolean isAndroidProject() {
        return isAndroidProject(project);
    }

    protected boolean isAndroidProject(Project project) {
        return project.getExtensions().findByName("android") != null;
    }
}
