package co.elastic.apm.android.plugin;

import com.android.build.api.artifact.MultipleArtifact;
import com.android.build.api.component.impl.ComponentImpl;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import com.android.build.gradle.BaseExtension;

import net.bytebuddy.build.gradle.android.ByteBuddyAndroidPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import java.util.Objects;

import co.elastic.apm.android.plugin.tasks.ApmInfoGenerator;
import co.elastic.apm.generated.BuildConfig;

class ApmAndroidAgentPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        addBytebuddyPlugin(project);
        addSdkDependency(project);
        addInstrumentationDependency(project);
        addApplicationInfoCreationTask(project);
    }

    private void addBytebuddyPlugin() {
        project.getPluginManager().apply(ByteBuddyAndroidPlugin.class);
    }

    private void addSdkDependency() {
        project.getDependencies().add("implementation", BuildConfig.SDK_DEPENDENCY_URI);
    }

    private void addInstrumentationDependency() {
        project.getDependencies().add("bytebuddy", BuildConfig.INSTRUMENTATION_DEPENDENCY_URI);
    }

    private void addApplicationInfoCreationTask() {
        ExtensionContainer extensions = project.getExtensions();
        ApplicationAndroidComponentsExtension extension = extensions.getByType(ApplicationAndroidComponentsExtension.class);
        BaseExtension androidExtension = extensions.getByType(BaseExtension.class);

        extension.onVariants(extension.selector().all(), applicationVariant -> {
            ComponentImpl component = (ComponentImpl) applicationVariant;
            String variantName = applicationVariant.getName();
            TaskProvider<ApmInfoGenerator> taskProvider = project.getTasks().register(variantName + "GenerateApmInfo", ApmInfoGenerator.class);
            taskProvider.configure(apmInfoGenerator -> {
                apmInfoGenerator.getVariantName().set(variantName);
                apmInfoGenerator.getVersion().set(androidExtension.getDefaultConfig().getVersionName());
                apmInfoGenerator.getOutputDir().set(project.getLayout().getBuildDirectory().dir(apmInfoGenerator.getName()));
                apmInfoGenerator.getOkHttpVersion().set(getOkhttpVersion(component));
            });

            applicationVariant.getArtifacts().use(taskProvider)
                    .wiredWith(ApmInfoGenerator::getOutputDir)
                    .toAppendTo(MultipleArtifact.ASSETS.INSTANCE);
        });
    }

    private Provider<String> getOkhttpVersion(ComponentImpl component) {
        return project.provider(() -> {
            DependencySet allDependencies = component.getVariantDependencies().getRuntimeClasspath().getAllDependencies();
            for (Dependency dependency : allDependencies) {
                if (Objects.equals(dependency.getGroup(), "com.squareup.okhttp3") && dependency.getName().equals("okhttp")) {
                    return dependency.getVersion();
                }
            }
            return null;
        });
    }
}