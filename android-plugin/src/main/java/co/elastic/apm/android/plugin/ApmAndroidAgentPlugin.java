package co.elastic.apm.android.plugin;

import net.bytebuddy.build.gradle.android.ByteBuddyAndroidPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import co.elastic.apm.generated.BuildConfig;

class ApmAndroidAgentPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        addBytebuddyPlugin(project);
        addSdkDependency(project);
        addInstrumentationDependency(project);
    }

    private void addBytebuddyPlugin(Project project) {
        project.getPluginManager().apply(ByteBuddyAndroidPlugin.class);
    }

    private void addSdkDependency(Project project) {
        project.getDependencies().add("implementation", BuildConfig.SDK_DEPENDENCY_URI);
    }

    private void addInstrumentationDependency(Project project) {
        project.getDependencies().add("bytebuddy", BuildConfig.INSTRUMENTATION_DEPENDENCY_URI);
    }
}