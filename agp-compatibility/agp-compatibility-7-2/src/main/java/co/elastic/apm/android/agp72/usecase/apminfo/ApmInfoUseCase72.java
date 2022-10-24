package co.elastic.apm.android.agp72.usecase.apminfo;

import com.android.build.api.artifact.MultipleArtifact;
import com.android.build.api.component.impl.ComponentImpl;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.android.agp.api.usecase.ApmInfoUseCase;

public abstract class ApmInfoUseCase72 extends ApmInfoUseCase {

    @Override
    public void execute() {
        Project project = getProject().get();
        AndroidComponentsExtension<?, ?, Variant> componentsExtension = project.getExtensions().getByType(AndroidComponentsExtension.class);

        componentsExtension.onVariants(componentsExtension.selector().all(), variant -> {
            addTaskToVariant(project, getParameters().get(), variant);
        });
    }

    private void addTaskToVariant(Project project, Parameters parameters, Variant variant) {
        String variantName = variant.getName();
        TaskProvider<ApmInfoGeneratorTask> taskProvider = project.getTasks().register(variantName + "GenerateApmInfo", ApmInfoGeneratorTask.class);
        taskProvider.configure(apmInfoGenerator -> {
            apmInfoGenerator.getServiceName().set(parameters.getServiceName());
            apmInfoGenerator.getServiceVersion().set(parameters.getServiceVersion());
            apmInfoGenerator.getServerUrl().set(parameters.getServerUrl());
            apmInfoGenerator.getVariantName().set(variantName);
            apmInfoGenerator.getOutputDir().set(project.getLayout().getBuildDirectory().dir(apmInfoGenerator.getName()));
            apmInfoGenerator.getOkHttpVersion().set(getOkhttpVersion(project, variant));
        });

        attachTaskToVariant(variant, taskProvider);
    }

    private void attachTaskToVariant(Variant variant, TaskProvider<ApmInfoGeneratorTask> taskProvider) {
        variant.getArtifacts().use(taskProvider)
                .wiredWith(ApmInfoGeneratorTask::getOutputDir)
                .toAppendTo(MultipleArtifact.ASSETS.INSTANCE);
    }

    private Provider<String> getOkhttpVersion(Project project, Variant variant) {
        ComponentImpl component = (ComponentImpl) variant;
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
