package co.elastic.apm.android.agp.api;

import org.gradle.api.Action;
import org.gradle.api.Project;

import co.elastic.apm.android.agp.api.usecase.ApmInfoUseCase;
import co.elastic.apm.android.agp.api.usecase.base.AgpUseCase;

public abstract class AgpCompatibilityManager {
    protected final Project project;

    protected AgpCompatibilityManager(Project project) {
        this.project = project;
    }

    protected <P extends AgpUseCase.Parameters, T extends AgpUseCase<P>> T createUseCase(Class<T> useCaseClass, Class<P> parametersClass, Action<P> config) {
        T useCase = project.getObjects().newInstance(useCaseClass);
        P parameters = project.getObjects().newInstance(parametersClass);
        if (config != null) {
            config.execute(parameters);
        }
        useCase.getParameters().set(parameters);
        useCase.getProject().set(project);
        return useCase;
    }

    public abstract ApmInfoUseCase getApmInfoUseCase(Action<ApmInfoUseCase.Parameters> config);
}
