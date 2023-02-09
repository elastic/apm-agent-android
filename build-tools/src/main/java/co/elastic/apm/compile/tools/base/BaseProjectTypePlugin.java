package co.elastic.apm.compile.tools.base;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class BaseProjectTypePlugin implements Plugin<Project> {

    protected Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPlugins().withId("com.android.library", plugin -> onAndroidLibraryFound());
        project.getPlugins().withId("java-library", plugin -> onJavaLibraryFound());
    }

    protected abstract void onAndroidLibraryFound();

    protected abstract void onJavaLibraryFound();
}
