package co.elastic.apm.android.plugin;

import com.android.build.api.artifact.MultipleArtifact;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import com.android.build.gradle.BaseExtension;

import net.bytebuddy.build.gradle.android.ByteBuddyAndroidPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.android.plugin.tasks.ApplicationInfoGenerator;
import co.elastic.apm.generated.BuildConfig;

class ApmAndroidAgentPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        addBytebuddyPlugin(project);
        addSdkDependency(project);
        addInstrumentationDependency(project);
        addApplicationInfoCreationTask(project);
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

    private void addApplicationInfoCreationTask(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        ApplicationAndroidComponentsExtension extension = extensions.getByType(ApplicationAndroidComponentsExtension.class);
        BaseExtension androidExtension = extensions.getByType(BaseExtension.class);

        extension.onVariants(extension.selector().all(), applicationVariant -> {
            String variantName = applicationVariant.getName();
            TaskProvider<ApplicationInfoGenerator> provider = project.getTasks()
                    .register(variantName + "CreateApplicationInfo", ApplicationInfoGenerator.class);

            provider.configure(applicationInfoGenerator -> {
                applicationInfoGenerator.getOutputDir().set(project.getLayout().getBuildDirectory().dir(applicationInfoGenerator.getName()));
                applicationInfoGenerator.getVariantName().set(variantName);
                applicationInfoGenerator.getVersion().set(androidExtension.getDefaultConfig().getVersionName());
            });

            applicationVariant.getArtifacts().use(provider)
                    .wiredWith(ApplicationInfoGenerator::getOutputDir)
                    .toAppendTo(MultipleArtifact.ALL_CLASSES_DIRS.INSTANCE);
        });
    }
}