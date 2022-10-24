package co.elastic.apm.android.agp.api;

import org.gradle.api.Project;

public abstract class AgpCompatibilityManager {
    protected final Project project;

    protected AgpCompatibilityManager(Project project) {
        this.project = project;
    }

    public abstract AgpUseCase getApmInfoUseCase();
}
