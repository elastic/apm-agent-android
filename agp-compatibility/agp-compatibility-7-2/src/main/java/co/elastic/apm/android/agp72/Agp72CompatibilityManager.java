package co.elastic.apm.android.agp72;

import org.gradle.api.Project;

import co.elastic.apm.android.agp.api.AgpCompatibilityManager;
import co.elastic.apm.android.agp.api.usecase.AgpUseCase;

public class Agp72CompatibilityManager extends AgpCompatibilityManager {

    protected Agp72CompatibilityManager(Project project) {
        super(project);
    }

    @Override
    public AgpUseCase getApmInfoUseCase() {
        return null;
    }
}
