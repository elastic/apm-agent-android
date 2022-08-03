package co.elastic.apm.android.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import co.elastic.apm.generated.BuildConfig;

class ApmAndroidAgentPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        addSdkDependency(project);
    }

    private void addSdkDependency(Project project) {
        project.getDependencies().add("implementation", BuildConfig.SDK_DEPENDENCY_URI);
    }
}