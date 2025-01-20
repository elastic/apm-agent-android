package co.elastic.otel.android.compilation.tools.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.tasks.Internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseTask extends DefaultTask {

    protected List<ComponentIdentifier> getComponentIdentifiers(List<Configuration> configurations) {
        Set<ComponentIdentifier> componentIdentifiers = new HashSet<>();

        for (Configuration dependencies : configurations) {
            componentIdentifiers.addAll(getComponentIdentifiers(dependencies));
        }

        return new ArrayList<>(componentIdentifiers);
    }

    protected List<ComponentIdentifier> getComponentIdentifiers(Configuration dependencies) {
        List<String> externalDependenciesIds = new ArrayList<>();

        for (Dependency dependency : dependencies.getAllDependencies()) {
            if (dependency instanceof ExternalModuleDependency) {
                ExternalModuleDependency moduleDependency = (ExternalModuleDependency) dependency;
                externalDependenciesIds.add(moduleDependency.getGroup() + ":" + moduleDependency.getName());
            }
        }

        Set<ResolvedArtifact> resolvedArtifacts = dependencies.getResolvedConfiguration().getResolvedArtifacts();
        List<ComponentIdentifier> identifiers = new ArrayList<>();

        for (ResolvedArtifact resolvedArtifact : resolvedArtifacts) {
            ModuleVersionIdentifier moduleId = resolvedArtifact.getModuleVersion().getId();
            String moduleIdName = moduleId.getGroup() + ":" + moduleId.getName();
            if (externalDependenciesIds.contains(moduleIdName)) {
                externalDependenciesIds.remove(moduleIdName);
                identifiers.add(resolvedArtifact.getId().getComponentIdentifier());
            }
        }

        if (!externalDependenciesIds.isEmpty()) {
            getLogger().warn("POM files not found for the following dependencies: " + externalDependenciesIds);
        }

        return identifiers;
    }

    @Internal
    protected boolean isAndroidProject() {
        return getProject().getExtensions().findByName("android") != null;
    }
}
