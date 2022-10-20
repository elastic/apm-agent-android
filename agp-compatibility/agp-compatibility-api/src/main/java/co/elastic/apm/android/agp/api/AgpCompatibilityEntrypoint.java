package co.elastic.apm.android.agp.api;

import com.android.build.api.AndroidPluginVersion;
import com.android.build.api.variant.AndroidComponentsExtension;

import org.gradle.api.Project;

import java.util.ServiceLoader;

import co.elastic.apm.android.common.internal.logging.Elog;

public interface AgpCompatibilityEntrypoint {
    String getIdentifier();

    boolean isCompatible(CurrentVersion currentVersion);

    AgpCompatibleUseCase provideCompatibleUseCase(Project project);

    static AgpCompatibleUseCase findCompatibleUseCase(Project project) {
        ServiceLoader<AgpCompatibilityEntrypoint> entrypoints = ServiceLoader.load(AgpCompatibilityEntrypoint.class);
        if (!entrypoints.iterator().hasNext()) {
            throw new IllegalStateException("No implementations found for " + AgpCompatibilityEntrypoint.class.getName());
        }

        AndroidPluginVersion currentVersion = project.getExtensions().findByType(AndroidComponentsExtension.class).getPluginVersion();
        CurrentVersion comparable = new CurrentVersion(currentVersion);

        for (AgpCompatibilityEntrypoint entrypoint : entrypoints) {
            if (entrypoint.isCompatible(comparable)) {
                Elog.getLogger().debug("Found AGP compatible entrypoint: {}", entrypoint.getIdentifier());
                return entrypoint.provideCompatibleUseCase(project);
            }
        }

        throw new UnsupportedOperationException();
    }
}
