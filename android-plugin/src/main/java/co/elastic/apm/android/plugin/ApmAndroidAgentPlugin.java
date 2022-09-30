package co.elastic.apm.android.plugin;

import com.android.build.api.artifact.MultipleArtifact;
import com.android.build.api.component.impl.ComponentImpl;
import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import com.android.build.api.variant.ApplicationVariant;
import com.android.build.gradle.BaseExtension;

import net.bytebuddy.build.gradle.android.ByteBuddyAndroidPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.plugin.instrumentation.ElasticLocalInstrumentationFactory;
import co.elastic.apm.android.plugin.logging.GradleLoggerFactory;
import co.elastic.apm.android.plugin.tasks.ApmInfoGenerator;
import co.elastic.apm.android.plugin.tasks.OkHttpEventlistenerGenerator;
import co.elastic.apm.generated.BuildConfig;
import kotlin.Unit;

class ApmAndroidAgentPlugin implements Plugin<Project> {

    private Project project;
    private BaseExtension androidExtension;

    @Override
    public void apply(Project project) {
        this.project = project;
        Elog.init(new GradleLoggerFactory());
        androidExtension = project.getExtensions().getByType(BaseExtension.class);
        addBytebuddyPlugin();
        addSdkDependency();
        addInstrumentationDependency();
        addTasks();
    }

    private void addBytebuddyPlugin() {
        project.getPluginManager().apply(ByteBuddyAndroidPlugin.class);
    }

    private void addSdkDependency() {
        project.getDependencies().add("implementation", BuildConfig.SDK_DEPENDENCY_URI);
        if (kotlinPluginFound()) {
            project.getDependencies().add("implementation", BuildConfig.SDK_KTX_DEPENDENCY_URI);
        }
    }

    private boolean kotlinPluginFound() {
        return project.getExtensions().findByName("kotlin") != null;
    }

    private void addInstrumentationDependency() {
        project.getDependencies().add("byteBuddy", BuildConfig.INSTRUMENTATION_DEPENDENCY_URI);
    }

    private void addTasks() {
        ExtensionContainer extensions = project.getExtensions();
        ApplicationAndroidComponentsExtension extension = extensions.getByType(ApplicationAndroidComponentsExtension.class);

        extension.onVariants(extension.selector().all(), this::enhanceVariant);
    }

    private void enhanceVariant(ApplicationVariant applicationVariant) {
        addApmInfoGenerator(applicationVariant);
        addOkhttpEventListenerGenerator(applicationVariant);
        addLocalRemapping(applicationVariant);
    }

    private void addLocalRemapping(ApplicationVariant applicationVariant) {
        applicationVariant.getInstrumentation().transformClassesWith(ElasticLocalInstrumentationFactory.class, InstrumentationScope.PROJECT, none -> Unit.INSTANCE);
    }

    private void addOkhttpEventListenerGenerator(ApplicationVariant applicationVariant) {
        ComponentImpl component = (ComponentImpl) applicationVariant;
        TaskProvider<OkHttpEventlistenerGenerator> taskProvider =
                project.getTasks().register(applicationVariant.getName() + "GenerateOkhttpEventListener", OkHttpEventlistenerGenerator.class);
        taskProvider.configure(task -> {
            task.getOutputDir().set(project.getLayout().getBuildDirectory().dir(task.getName()));
            task.getAppRuntimeClasspath().from(component.getVariantDependencies().getRuntimeClasspath());
            task.getJvmTargetVersion().set(androidExtension.getCompileOptions().getTargetCompatibility().toString());
        });
        applicationVariant.getArtifacts().use(taskProvider)
                .wiredWith(OkHttpEventlistenerGenerator::getOutputDir)
                .toAppendTo(MultipleArtifact.ALL_CLASSES_DIRS.INSTANCE);
    }

    private void addApmInfoGenerator(ApplicationVariant applicationVariant) {
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
    }

    private Provider<String> getOkhttpVersion(ComponentImpl component) {
        return project.provider(() -> {
            Configuration runtimeClasspath = component.getVariantDependencies().getRuntimeClasspath();
            ResolvedConfiguration resolvedConfiguration = runtimeClasspath.getResolvedConfiguration();
            try {
                for (ResolvedArtifact artifact : resolvedConfiguration.getResolvedArtifacts()) {
                    ModuleVersionIdentifier identifier = artifact.getModuleVersion().getId();
                    if (identifier.getGroup().equals("com.squareup.okhttp3") && identifier.getName().equals("okhttp")) {
                        return identifier.getVersion();
                    }
                }
            } catch (ResolveException ignored) {
            }
            return null;
        });
    }
}