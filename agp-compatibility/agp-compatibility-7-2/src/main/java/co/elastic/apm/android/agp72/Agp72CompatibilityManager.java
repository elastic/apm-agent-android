package co.elastic.apm.android.agp72;

import org.gradle.api.Action;
import org.gradle.api.Project;

import co.elastic.apm.android.agp.api.AgpCompatibilityManager;
import co.elastic.apm.android.agp.api.usecase.ApmInfoUseCase;

public class Agp72CompatibilityManager extends AgpCompatibilityManager {

    protected Agp72CompatibilityManager(Project project) {
        super(project);
    }

    @Override
    public ApmInfoUseCase getApmInfoUseCase(Action<ApmInfoUseCase.Parameters> config) {
        return createUseCase(ApmInfoUseCase.class, ApmInfoUseCase.Parameters.class, config);
    }
}
