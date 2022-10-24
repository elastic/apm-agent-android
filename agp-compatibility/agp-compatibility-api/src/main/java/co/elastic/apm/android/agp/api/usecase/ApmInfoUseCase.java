package co.elastic.apm.android.agp.api.usecase;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import co.elastic.apm.android.agp.api.usecase.base.AgpUseCase;

public abstract class ApmInfoUseCase extends AgpUseCase<ApmInfoUseCase.Parameters> {

    public abstract static class Parameters implements AgpUseCase.Parameters {
        @Input
        public abstract Provider<String> getServiceName();

        @Input
        public abstract Property<String> getVariantName();

        @Input
        public abstract Property<String> getServerUrl();

        @Input
        public abstract Property<String> getServiceVersion();

        @Optional
        @Input
        public abstract Property<String> getServerToken();
    }
}
