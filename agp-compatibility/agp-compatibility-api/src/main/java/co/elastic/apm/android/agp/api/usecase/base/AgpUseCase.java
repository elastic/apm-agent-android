package co.elastic.apm.android.agp.api.usecase.base;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;

public abstract class AgpUseCase<T extends AgpUseCase.Parameters> {

    @Input
    public abstract Property<Project> getProject();

    @Nested
    public abstract Property<T> getParameters();

    public abstract void execute();

    public interface Parameters {
        class None implements Parameters {
        }
    }
}
